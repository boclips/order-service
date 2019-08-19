package com.boclips.terry.application

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderItemLicense
import com.boclips.eventbus.events.order.LegacyOrderNextStatus
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.infrastructure.orders.FakeLegacyOrdersRepository
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.service.client.CreateContentPartnerRequest
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories
import java.math.BigDecimal
import java.util.Date

class OrderPersistenceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var eventBus: SynchronousFakeEventBus

    @Autowired
    lateinit var legacyOrdersRepository: FakeLegacyOrdersRepository

    @BeforeEach
    fun setUp() {
        fakeOrdersRepository.clear()
        legacyOrdersRepository.clear()
        fakeVideoClient.setUseInternalProjection(true)
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

        val contentPartnerId =
            fakeVideoClient.createContentPartner(CreateContentPartnerRequest.builder().name("ted").build())

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                title = "hippos are cool",
                providerId = contentPartnerId.value,
                contentType = VideoType.NEWS
            )
        )

        val items = singularItemList(
            createdAt = item1CreatedAt,
            updatedAt = item1UpdatedAt,
            licenseCreatedAt = license1CreatedAt,
            licenseUpdatedAt = license1UpdatedAt,
            uuid = "item-1-uuid",
            assetId = videoId.value
        )

        eventBus.publish(
            legacyOrderSubmitted(
                legacyOrder = legacyOrder,
                items = items,
                creator = "creator@their-company.net",
                vendor = "vendor@their-company.biz"
            )
        )

        val orders = fakeOrdersRepository.findAll()

        assertThat(orders).hasSize(1)

        val order = orders.first()
        assertThat(order.id).isNotNull()

        assertThat(order.orderProviderId).isEqualTo(legacyOrder.id)
        assertThat(order.createdAt).isEqualTo(orderCreatedAt.toInstant())
        assertThat(order.updatedAt).isEqualTo(orderUpdatedAt.toInstant())
        assertThat(order.creatorEmail).isEqualTo("creator@their-company.net")
        assertThat(order.vendorEmail).isEqualTo("vendor@their-company.biz")
        assertThat(order.isbnOrProductNumber).isEqualTo("some-isbn")
        assertThat(order.status).isEqualTo(OrderStatus.CONFIRMED)

        assertThat(order.items.size).isEqualTo(1)

        val item = order.items.first()
        assertThat(item.uuid).isEqualTo("item-1-uuid")
        assertThat(item.price).isEqualTo(BigDecimal.ONE)
        assertThat(item.transcriptRequested).isEqualTo(true)
        assertThat(item.contentPartner.referenceId.value).isEqualTo(contentPartnerId.value)
        assertThat(item.contentPartner.name).isEqualTo("ted")
        assertThat(item.video.referenceId.value).isEqualTo(videoId.value)
        assertThat(item.video.title).isEqualTo("hippos are cool")
        assertThat(item.video.type).isEqualTo(VideoType.NEWS.name)

        assertThat(legacyOrdersRepository.findById(OrderId(legacyOrder.id)))
            .isEqualTo(
                LegacyOrderDocument(
                    order = legacyOrder,
                    items = items,
                    creator = "creator@their-company.net",
                    vendor = "vendor@their-company.biz"
                )
            )
    }

    private fun singularItemList(
        createdAt: Date,
        updatedAt: Date,
        licenseCreatedAt: Date,
        licenseUpdatedAt: Date,
        uuid: String,
        assetId: String
    ): List<LegacyOrderItem> = listOf(
        LegacyOrderItem
            .builder()
            .id(ObjectId.get().toHexString())
            .uuid(uuid)
            .assetId(assetId)
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

    private fun legacyOrderSubmitted(
        legacyOrder: LegacyOrder,
        items: List<LegacyOrderItem>,
        creator: String,
        vendor: String
    ) =
        LegacyOrderSubmitted.builder()
            .order(legacyOrder)
            .orderItems(items)
            .creator(creator)
            .vendor(vendor)
            .build()
}
