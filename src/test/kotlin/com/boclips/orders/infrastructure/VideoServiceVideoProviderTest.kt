package com.boclips.orders.infrastructure

import com.boclips.orders.domain.exceptions.MissingCurrencyForChannel
import com.boclips.orders.domain.exceptions.MissingVideoFullProjectionLink
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import java.util.Currency

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
                captionStatus = CaptionStatus.HUMAN_GENERATED_AVAILABLE,
                playback = StreamPlaybackResource(
                    id = "playback-id",
                    referenceId = "reference-id",
                    maxResolutionAvailable = true
                ),
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
        assertThat(video.captionStatus).isEqualTo(AssetStatus.AVAILABLE)
        assertThat(video.downloadableVideoStatus).isEqualTo(AssetStatus.AVAILABLE)
        assertThat(video.captionAdminLink.toString()).isEqualTo("https://kmc.kaltura.com/index.php/kmcng/content/entries/entry/playback-id/metadata")
        assertThat(video.videoUploadLink.toString()).isEqualTo("https://kmc.kaltura.com/index.php/kmcng/content/entries/entry/playback-id/flavours")
    }

    @Test
    fun `throws when channel id is missing`() {
        val videoId = fakeVideoClient.add(
            VideoResource(
                id = "video-id",
                title = "hello",
                createdBy = "our content partner",
                captionStatus = CaptionStatus.HUMAN_GENERATED_AVAILABLE,
                playback = StreamPlaybackResource(
                    id = "playback-id",
                    referenceId = "reference-id",
                    maxResolutionAvailable = true
                ),
                channelId = null,
                channelVideoId = "",
                _links = mapOf(
                    "fullProjection" to HateoasLink("https://great-vids.com")
                )
            )
        )

        assertThrows<IllegalStateException> { videoProvider.get(videoId = VideoId(value = videoId.id!!)) }
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
