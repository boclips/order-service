package com.boclips.terry.application

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderNextStatus
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.TrimRequest
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

class OrderPersistenceIntegrationTest : AbstractSpringIntegrationTest() {
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

        val items = listOf(
            TestFactories.legacyOrderItem(
                dateCreated = item1CreatedAt,
                dateUpdated = item1UpdatedAt,
                uuid = "item-1-uuid",
                assetId = videoId.value,
                price = BigDecimal.ONE,
                transcriptsRequired = true,
                trimming = "40 - 100"
            )
        )

        val genericUser = TestFactories.legacyOrderUser(
            firstName = "Steve",
            lastName = "Jobs",
            userName = "macs",
            id = "123",
            email = "123@macs.com",
            organisation = TestFactories.legacyOrderOrganisation(
                name = "organisation",
                id = "456"
            )
        )

        eventBus.publish(
            legacyOrderSubmitted(
                legacyOrder = legacyOrder,
                items = items,
                creator = "creator@their-company.net",
                vendor = "vendor@their-company.biz",
                requestingUser = genericUser,
                authorisingUser = genericUser
            )
        )

        val orders = fakeOrdersRepository.findAll()

        assertThat(orders).hasSize(1)

        val order = orders.first()
        assertThat(order.id).isNotNull

        assertThat(order.orderProviderId).isEqualTo(legacyOrder.id)
        assertThat(order.createdAt).isEqualTo(orderCreatedAt.toInstant())
        assertThat(order.updatedAt).isEqualTo(orderUpdatedAt.toInstant())
        assertThat(order.requestingUser).isEqualTo(
            TestFactories.orderUser(
                firstName = "Steve",
                lastName = "Jobs",
                sourceUserId = "123",
                email = "123@macs.com",
                organisation = TestFactories.orderOrganisation(name = "organisation", sourceOrganisationId = "456")
            )
        )
        assertThat(order.authorisingUser).isEqualTo(
            TestFactories.orderUser(
                firstName = "Steve",
                lastName = "Jobs",
                sourceUserId = "123",
                email = "123@macs.com",
                organisation = TestFactories.orderOrganisation(name = "organisation", sourceOrganisationId = "456")
            )
        )
        assertThat(order.isbnOrProductNumber).isEqualTo("some-isbn")
        assertThat(order.status).isEqualTo(OrderStatus.CONFIRMED)

        assertThat(order.items.size).isEqualTo(1)

        val item = order.items.first()
        assertThat(item.uuid).isEqualTo("item-1-uuid")
        assertThat(item.price).isEqualTo(BigDecimal.ONE)
        assertThat(item.transcriptRequested).isEqualTo(true)
        assertThat(item.trim).isEqualTo(TrimRequest.WithTrimming("40 - 100"))
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
                    vendor = "vendor@their-company.biz",
                    authorisingUser = genericUser,
                    requestingUser = genericUser
                )
            )
    }

    private fun legacyOrder(
        orderId: String,
        orderCreatedAt: Date,
        orderUpdatedAt: Date,
        vendorUuid: String,
        creatorUuid: String
    ): LegacyOrder = TestFactories.legacyOrder(
        id = orderId,
        uuid = "deadb33f-f33df00d-d00fb3ad-c00bfeed",
        vendor = vendorUuid,
        creator = creatorUuid,
        dateCreated = orderCreatedAt,
        dateUpdated = orderUpdatedAt,
        legacyOrderExtraFields = LegacyOrderExtraFields
            .builder()
            .agreeTerms(true)
            .isbnOrProductNumber("some-isbn")
            .build(),
        legacyOrderNextStatus = LegacyOrderNextStatus
            .builder()
            .nextStates(listOf("GOOD", "BAD"))
            .roles(listOf("jam", "vegan-sausage"))
            .build(),
        status = "CONFIRMED"
    )

    private fun legacyOrderSubmitted(
        legacyOrder: LegacyOrder,
        items: List<LegacyOrderItem>,
        creator: String,
        vendor: String,
        authorisingUser: LegacyOrderUser,
        requestingUser: LegacyOrderUser
    ) =
        LegacyOrderSubmitted.builder()
            .order(legacyOrder)
            .orderItems(items)
            .creator(creator)
            .vendor(vendor)
            .authorisingUser(authorisingUser)
            .requestingUser(requestingUser)
            .build()
}
