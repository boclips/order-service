package com.boclips.terry.application.orders

import com.boclips.eventbus.events.order.LegacyOrder
import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.Territory
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
import java.time.temporal.ChronoUnit
import java.util.Date

class StoreLegacyOrderIntegrationTest : AbstractSpringIntegrationTest() {
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
        val date = Date()
        val orderId = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(
            id = orderId,
            dateCreated = date,
            dateUpdated = date,
            vendor = "illegible-vendor-uuid",
            creator = "illegible-creator-uuid",
            legacyOrderExtraFields = LegacyOrderExtraFields.builder()
                .agreeTerms(true)
                .isbnOrProductNumber("some-isbn")
                .build(),
            status = "CONFIRMED"
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
                dateCreated = date,
                dateUpdated = date,
                uuid = "item-1-uuid",
                assetId = videoId.value,
                price = BigDecimal.ONE,
                transcriptsRequired = true,
                trimming = "40 - 100",
                license = TestFactories.legacyOrderItemLicense(code = "10YR_SR")
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
        assertThat(order.legacyOrderId).isEqualTo(legacyOrder.id)
        assertThat(order.createdAt).isEqualTo(date.toInstant())
        assertThat(order.updatedAt).isEqualTo(date.toInstant())
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
        assertThat(item.license.duration.amount).isEqualTo(10)
        assertThat(item.license.duration.unit).isEqualTo(ChronoUnit.YEARS)
        assertThat(item.license.territory).isEqualTo(Territory.SINGLE_REGION)
        assertThat(item.contentPartner.videoServiceId.value).isEqualTo(contentPartnerId.value)
        assertThat(item.contentPartner.name).isEqualTo("ted")
        assertThat(item.video.videoServiceId.value).isEqualTo(videoId.value)
        assertThat(item.video.title).isEqualTo("hippos are cool")
        assertThat(item.video.type).isEqualTo(VideoType.NEWS.name)
    }

    @Test
    fun `dumps all legacy order data from an event`() {
        val legacyOrder = TestFactories.legacyOrder()
        val genericUser = TestFactories.legacyOrderUser()
        val contentPartnerId =
            fakeVideoClient.createContentPartner(CreateContentPartnerRequest.builder().name("ted").build())

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                providerId = contentPartnerId.value
            )
        )
        val items = listOf(TestFactories.legacyOrderItem(assetId = videoId.value))

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
