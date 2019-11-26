package com.boclips.terry.application.orders

import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.TrimRequest
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.service.client.ContentPartner
import com.boclips.videos.service.client.ContentPartnerId
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.Date

class StoreLegacyOrderIntegrationTest : AbstractSpringIntegrationTest() {

    @BeforeEach
    fun setUp() {
        ordersRepository.deleteAll()
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
            legacyOrderExtraFields = LegacyOrderExtraFields.builder()
                .agreeTerms(true)
                .isbnOrProductNumber("some-isbn")
                .build(),
            status = "CONFIRMED"
        )

        fakeVideoClient.createContentPartner(
            ContentPartner.builder()
                .contentPartnerId(ContentPartnerId("ted-id"))
                .name("ted").currency(
                    Currency
                        .getInstance("GBP")
                )
                .build()
        )

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                title = "hippos are cool",
                providerId = "ted-id",
                contentType = VideoType.NEWS
            )
        )

        val items = listOf(
            TestFactories.legacyOrderItem(
                dateCreated = date,
                dateUpdated = date,
                uuid = "item-1-uuid",
                assetId = videoId.value,
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
            TestFactories.legacyOrderSubmitted(
                legacyOrder = legacyOrder,
                legacyOrderItems = items,
                requestingUser = genericUser,
                authorisingUser = genericUser
            )
        )

        val orders = ordersRepository.findAll()

        assertThat(orders).hasSize(1)

        val order = orders.first()
        assertThat(order.id).isNotNull
        assertThat(order.legacyOrderId).isEqualTo(legacyOrder.id)
        assertThat(order.createdAt).isEqualTo(date.toInstant())
        assertThat(order.updatedAt).isEqualTo(date.toInstant())
        assertThat(order.requestingUser).isEqualTo(
            OrderFactory.completeOrderUser(
                firstName = "Steve",
                lastName = "Jobs",
                sourceUserId = "123",
                email = "123@macs.com"
            )
        )
        assertThat(order.authorisingUser).isEqualTo(
            OrderFactory.completeOrderUser(
                firstName = "Steve",
                lastName = "Jobs",
                sourceUserId = "123",
                email = "123@macs.com"
            )
        )
        assertThat(order.isbnOrProductNumber).isEqualTo("some-isbn")
        assertThat(order.status).isEqualTo(OrderStatus.INCOMPLETED)

        assertThat(order.items.size).isEqualTo(1)
        val item = order.items.first()
        assertThat(item.price.amount).isNull()
        assertThat(item.transcriptRequested).isEqualTo(true)
        assertThat(item.trim).isEqualTo(TrimRequest.WithTrimming("40 - 100"))
        assertThat(item.license?.duration).isNull()
        assertThat(item.license?.territory).isNull()
        assertThat(item.video.contentPartner.videoServiceId.value).isEqualTo("ted-id")
        assertThat(item.video.contentPartner.name).isEqualTo("ted")
        assertThat(item.video.videoServiceId.value).isEqualTo(videoId.value)
        assertThat(item.video.title).isEqualTo("hippos are cool")
        assertThat(item.video.type).isEqualTo(VideoType.NEWS.name)
    }

    @Test
    fun `dumps all legacy order data from an event`() {
        val legacyOrder = TestFactories.legacyOrder()
        val genericUser = TestFactories.legacyOrderUser()
        fakeVideoClient.createContentPartner(
            ContentPartner.builder()
                .contentPartnerId(ContentPartnerId("ted-id"))
                .name("ted").currency(
                    Currency
                        .getInstance("GBP")
                )
                .build()
        )

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                providerId = "ted-id"
            )
        )
        val items = listOf(TestFactories.legacyOrderItem(assetId = videoId.value))

        eventBus.publish(
            TestFactories.legacyOrderSubmitted(
                legacyOrder = legacyOrder,
                legacyOrderItems = items,
                requestingUser = genericUser,
                authorisingUser = genericUser
            )
        )

        val legacyDocuments = legacyOrdersRepository.findAll()
        assertThat(legacyDocuments).hasSize(1)
        assertThat(legacyDocuments.first())
            .isEqualTo(
                LegacyOrderDocument(
                    order = legacyOrder,
                    items = items,
                    authorisingUser = genericUser,
                    requestingUser = genericUser
                )
            )
    }

    @Test
    fun `updates pre-existing orders`() {
        val order = OrderFactory.order()
        ordersRepository.save(order)

        val genericUser = TestFactories.legacyOrderUser()
        fakeVideoClient.createContentPartner(
            ContentPartner.builder()
                .contentPartnerId(ContentPartnerId("ted-id"))
                .name("ted").currency(
                    Currency
                        .getInstance("GBP")
                )
                .build()
        )

        val videoId = fakeVideoClient.createVideo(
            TestFactories.createVideoRequest(
                providerId = "ted-id"
            )
        )
        val items = listOf(TestFactories.legacyOrderItem(assetId = videoId.value))

        eventBus.publish(
            TestFactories.legacyOrderSubmitted(
                legacyOrder = TestFactories.legacyOrder(id = order.legacyOrderId, status = "CANCELLED"),
                legacyOrderItems = items,
                authorisingUser = genericUser,
                requestingUser = genericUser
            )
        )

        val orders = ordersRepository.findAll()
        assertThat(orders).hasSize(1)
        assertThat(orders.first().status).isEqualTo(OrderStatus.CANCELLED)
    }
}
