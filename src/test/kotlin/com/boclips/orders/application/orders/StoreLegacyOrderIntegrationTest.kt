package com.boclips.orders.application.orders

import com.boclips.eventbus.events.order.LegacyOrderExtraFields
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.api.request.video.PlaybackResource
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import testsupport.TestFactories
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Currency

class StoreLegacyOrderIntegrationTest : AbstractSpringIntegrationTest() {

    @BeforeEach
    fun setUp() {
        ordersRepository.deleteAll()
        legacyOrdersRepository.clear()
    }

    @Test
    fun `persists legacy-orders when they arrive`() {
        val date = ZonedDateTime.now(ZoneOffset.UTC)
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

        fakeChannelsClient.add(
            ChannelResource(
                id = "ted-id",
                name = "ted",
                currency = Currency.getInstance("GBP").currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoResource = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hippos are cool",
                createdBy = "ted",
                channelId = "ted-id",
                channelVideoId = "",
                type = VideoTypeResource(id = 1, name = "NEWS"),
                _links = mapOf("fullProjection" to HateoasLink("https://great-vids.com")),
                playback = StreamPlaybackResource(id = "123", referenceId = "123")
            )
        )

        val items = listOf(
            TestFactories.legacyOrderItem(
                dateCreated = date,
                dateUpdated = date,
                uuid = "item-1-uuid",
                assetId = videoResource.id!!,
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
        assertThat(item.video.channel.videoServiceId.value).isEqualTo("ted-id")
        assertThat(item.video.channel.name).isEqualTo("ted")
        assertThat(item.video.videoServiceId.value).isEqualTo("video-id")
        assertThat(item.video.title).isEqualTo("hippos are cool")
        assertThat(item.video.type).isEqualTo("NEWS")
    }

    @Test
    fun `dumps all legacy order data from an event`() {
        val legacyOrder = TestFactories.legacyOrder()
        val genericUser = TestFactories.legacyOrderUser()

        fakeChannelsClient.add(
            ChannelResource(
                id = "ted-id",
                name = "ted",
                currency = Currency.getInstance("GBP").currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoResource = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "our content partne",
                channelId = "ted-id",
                channelVideoId = "",
                playback = StreamPlaybackResource(id = "123", referenceId = "123"),
                _links = mapOf("fullProjection" to HateoasLink("https://great-vids.com"))
            )
        )

        val items = listOf(TestFactories.legacyOrderItem(assetId = videoResource.id!!))

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

        fakeChannelsClient.add(
            ChannelResource(
                id = "ted-id",
                name = "ted",
                currency = Currency.getInstance("GBP").currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoResource = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                playback = StreamPlaybackResource(
                    id = "123",
                    referenceId = "123"
                ),
                createdBy = "our content partne",
                channelId = "ted-id",
                channelVideoId = "",
                _links = mapOf("fullProjection" to HateoasLink("https://great-vids.com"))
            )
        )
        val items = listOf(TestFactories.legacyOrderItem(assetId = videoResource.id!!))

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
