package com.boclips.orders.infrastructure

import com.boclips.orders.domain.exceptions.MissingCurrencyForChannel
import com.boclips.orders.domain.exceptions.MissingVideoFullProjectionLink
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.VideoResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import java.util.*

internal class VideoServiceVideoProviderTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var videoProvider: VideoProvider

    @Test
    fun `can get a video`() {
        val channel = fakeChannelsClient.add(
            ChannelResource(
                id = "cp-id",
                name = "our content partner",
                currency = Currency.getInstance("GBP").currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoId = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "our content partner",
                channelId = channel.id,
                channelVideoId = "",
                _links = mapOf(
                    "fullProjection" to HateoasLink("https://great-vids.com")
                )
            )
        )

        val video = videoProvider.get(videoId = VideoId(value = videoId.id!!))

        assertThat(video.title).isEqualTo("hello")
        assertThat(video.channel.name).isEqualTo("our content partner")
        assertThat(video.fullProjectionLink).describedAs("https://great-vids.com")

    }

    @Test
    fun `exception if video cannot be found`() {
        assertThrows<VideoNotFoundException> {
            videoProvider.get(videoId = VideoId(value = "hideandseek"))
        }
    }

    @Test
    fun `exception if content partner has no currency defined`() {
        fakeChannelsClient.add(
            ChannelResource(
                id = "cp-id",
                name = "our content partner",
                currency = null,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoResource = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "creator",
                channelId = "cp-id",
                channelVideoId = "x",
                _links = null
            )
        )

        assertThrows<MissingCurrencyForChannel> {
            videoProvider.get(videoId = VideoId(value = videoResource.id!!))
        }
    }

    @Test
    fun `exception if full projection link is missing`() {
        fakeChannelsClient.add(
            ChannelResource(
                id = "cp-id",
                name = "our content partner",
                currency = Currency.getInstance("GBP").currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )

        val videoResource = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "creator",
                channelId = "cp-id",
                channelVideoId = "x",
                _links = null
            )
        )

        assertThrows<MissingVideoFullProjectionLink> {
            videoProvider.get(videoId = VideoId(value = videoResource.id!!))
        }
    }
}
