package com.boclips.terry.application

import com.boclips.events.config.Subscriptions
import com.boclips.events.types.LegacyOrder
import com.boclips.events.types.LegacyOrderExtraFields
import com.boclips.events.types.LegacyOrderItem
import com.boclips.events.types.LegacyOrderItemLicense
import com.boclips.events.types.LegacyOrderNextStatus
import com.boclips.events.types.LegacyOrderSubmitted
import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.infrastructure.LegacyOrderDocument
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
        val legacyOrder = legacyOrder(orderId, orderCreatedAt, orderUpdatedAt)
        val items = singularItemList(item1CreatedAt, item1UpdatedAt, license1CreatedAt, license1UpdatedAt)

        subscriptions.legacyOrderSubmissions()
            .send(message(legacyOrder, items))

        assertThat(repo.findAll())
            .containsExactly(
                Order(
                    id = legacyOrder.id,
                    uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
                    createdAt = orderCreatedAt.toInstant(),
                    updatedAt = orderUpdatedAt.toInstant(),
                    vendor = "boclips",
                    creator = "big-bang",
                    isbnOrProductNumber = "some-isbn",
                    status = OrderStatus.CONFIRMED
                )
            )
        assertThat(repo.documentForOrderId(legacyOrder.id))
            .isEqualTo(
                LegacyOrderDocument(
                    order = legacyOrder,
                    items = items
                )
            )
    }

    @Test
    fun `well-formed messages with processing problems throw exceptions, to enqueue a retry`() {
        assertThrows<Exception> {
            subscriptions.legacyOrderSubmissions()
                .send(
                    message(
                        legacyOrder("please-throw", Date(0), Date(1)),
                        singularItemList(Date(2), Date(3), Date(4), Date(5))
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
        item1CreatedAt: Date,
        item1UpdatedAt: Date,
        license1CreatedAt: Date,
        license1UpdatedAt: Date
    ): List<LegacyOrderItem> = listOf(
        LegacyOrderItem
            .builder()
            .id("item-1-id")
            .uuid("item-1-uuid")
            .assetId("item-1-assetid")
            .dateCreated(item1CreatedAt)
            .dateUpdated(item1UpdatedAt)
            .license(
                LegacyOrderItemLicense
                    .builder()
                    .id("license1-id")
                    .uuid("license1-uuid")
                    .code("license1-code")
                    .description("license to kill")
                    .dateCreated(license1CreatedAt)
                    .dateUpdated(license1UpdatedAt)
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
        orderUpdatedAt: Date
    ): LegacyOrder = LegacyOrder
        .builder()
        .id(orderId)
        .uuid("deadb33f-f33df00d-d00fb3ad-c00bfeed")
        .vendor("boclips")
        .creator("big-bang")
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
        items: List<LegacyOrderItem>
    ): Message<LegacyOrderSubmitted> = MessageBuilder.withPayload(
        LegacyOrderSubmitted.builder()
            .order(legacyOrder)
            .orderItems(items)
            .build()
    )
        .build()
}
