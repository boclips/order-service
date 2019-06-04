package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.events.types.LegacyOrderSubmitted
import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderItem
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.infrastructure.LegacyOrderDocument
import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.Message
import org.springframework.messaging.support.GenericMessage
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.math.BigDecimal
import java.util.Date

@ExtendWith(SpringExtension::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderPersistenceTest {
    @Autowired
    lateinit var subscriptions: Subscriptions

    @Autowired
    lateinit var repo: FakeOrdersRepository

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `persists legacy-orders when they arrive`() {
        val orderCreatedAt = Date(0)
        val orderUpdatedAt = Date(1)
        val item1CreatedAt = Date(2)
        val item1UpdatedAt = Date(3)
        val license1CreatedAt = Date(4)
        val license1UpdatedAt = Date(5)
        val orderId = ObjectId().toHexString()
        val legacyOrder = legacyOrder(
            orderId = orderId,
            orderCreatedAt = orderCreatedAt,
            orderUpdatedAt = orderUpdatedAt,
            vendorUuid = "illegible-vendor-uuid",
            creatorUuid = "illegible-creator-uuid"
        )
        val items = singularItemList(
            createdAt = item1CreatedAt,
            updatedAt = item1UpdatedAt,
            licenseCreatedAt = license1CreatedAt,
            licenseUpdatedAt = license1UpdatedAt,
            uuid = "item-1-uuid"
        )

        subscriptions.legacyOrderSubmissions()
            .send(
                message(
                    legacyOrder = legacyOrder,
                    items = items,
                    creator = "creator@their-company.net",
                    vendor = "vendor@their-company.biz"
                )
            )

        assertThat(repo.findAll())
            .containsExactly(
                Order(
                    id = legacyOrder.id,
                    uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
                    createdAt = orderCreatedAt.toInstant(),
                    updatedAt = orderUpdatedAt.toInstant(),
                    creatorEmail = "creator@their-company.net",
                    vendorEmail = "vendor@their-company.biz",
                    isbnOrProductNumber = "some-isbn",
                    status = OrderStatus.CONFIRMED,
                    items = listOf(
                        OrderItem(uuid = "item-1-uuid", price = BigDecimal.ONE, transcriptRequested = true)
                    )
                )
            )
        assertThat(repo.documentForOrderId(legacyOrder.id))
            .isEqualTo(
                LegacyOrderDocument(
                    order = legacyOrder,
                    items = items,
                    creator = "creator@their-company.net",
                    vendor = "vendor@their-company.biz"
                )
            )
    }

    @Test
    fun `well-formed messages with processing problems throw exceptions, to enqueue a retry`() {
        assertThrows<Exception> {
            subscriptions.legacyOrderSubmissions()
                .send(
                    message(
                        legacyOrder = legacyOrder("please-throw", Date(0), Date(1), "deadb33f", "f33df00d"),
                        items = singularItemList(Date(2), Date(3), Date(4), Date(5), "item-1-uuid"),
                        creator = "hi@there.eu",
                        vendor = "bye@there.eu"
                    )
                )
        }

        assertThat(repo.findAll())
            .isEmpty()
    }

    @Test
    fun `malformed messages don't create orders, and are removed from the queue`() {
        subscriptions.legacyOrderSubmissions()
            .send(GenericMessage("{}"))

        assertThat(repo.findAll())
            .isEmpty()
    }

    private fun singularItemList(
        createdAt: Date,
        updatedAt: Date,
        licenseCreatedAt: Date,
        licenseUpdatedAt: Date,
        uuid: String
    ): List<LegacyOrderItem> = listOf(
        LegacyOrderItem
            .builder()
            .id("item-1-id")
            .uuid(uuid)
            .assetId("item-1-assetid")
            .dateCreated(createdAt)
            .dateUpdated(updatedAt)
            .license(
                LegacyOrderItemLicense
                    .builder()
                    .id("license1-id")
                    .uuid("license1-uuid")
                    .code("license1-code")
                    .description("license to kill")
                    .dateCreated(licenseCreatedAt)
                    .dateUpdated(licenseUpdatedAt)
                    .build()
            )
            .price(BigDecimal.ONE)
            .transcriptsRequired(true)
            .status("KINGOFITEMS")
            .build()
    )

    private fun legacyOrder(
        orderId: String,
        orderCreatedAt: Date,
        orderUpdatedAt: Date,
        vendorUuid: String,
        creatorUuid: String
    ): LegacyOrder = LegacyOrder
        .builder()
        .id(orderId)
        .uuid("deadb33f-f33df00d-d00fb3ad-c00bfeed")
        .vendor(vendorUuid)
        .creator(creatorUuid)
        .dateCreated(orderCreatedAt)
        .dateUpdated(orderUpdatedAt)
        .extraFields(
            LegacyOrderExtraFields
                .builder()
                .agreeTerms(true)
                .isbnOrProductNumber("some-isbn")
                .build()
        )
        .nextStatus(
            LegacyOrderNextStatus
                .builder()
                .nextStates(listOf("GOOD", "BAD"))
                .roles(listOf("jam", "vegan-sausage"))
                .build()
        )
        .status("CONFIRMED")
        .build()

    private fun message(
        legacyOrder: LegacyOrder,
        items: List<LegacyOrderItem>,
        creator: String,
        vendor: String
    ): Message<LegacyOrderSubmitted> = MessageBuilder.withPayload(
        LegacyOrderSubmitted.builder()
            .order(legacyOrder)
            .orderItems(items)
            .creator(creator)
            .vendor(vendor)
            .build()
    )
        .build()
}
