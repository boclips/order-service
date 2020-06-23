package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import testsupport.TestFactories

class SyncVideosIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var syncVideos: SyncVideos

    @Test
    fun `it updates an order items's video`() {
        val oldVideo = TestFactories.video(
            captionStatus = AssetStatus.PROCESSING,
            downloadableVideoStatus = AssetStatus.PROCESSING
        )
        val order = saveOrder(
            OrderFactory.completeOrder(
                items = listOf(
                    OrderFactory.orderItem(video = oldVideo)
                )
            )
        )

        fakeVideoClient.add(VideoResource(
            id = oldVideo.videoServiceId.value,
            title = "hello",
            channelId = "123",
            channelVideoId = "123",
            createdBy = "Keyser Söze",
            captionStatus = CaptionStatus.AVAILABLE,
            playback = StreamPlaybackResource(id = oldVideo.playbackId, referenceId = oldVideo.playbackId),
            _links = mapOf("fullProjection" to HateoasLink(href = "https://hello.org"))
        ))

        fakeChannelsClient.add(ChannelResource(id = "123", name = "the real mvp", currency = "GBP"))

        syncVideos()

        val retrievedOrder = ordersRepository.findOne(order.id)!!
        assertThat(retrievedOrder.items.first().video.captionStatus).isEqualTo(AssetStatus.AVAILABLE)
    }
}
