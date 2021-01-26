package com.boclips.orders.infrastructure

import com.boclips.orders.domain.FetchVideoResourceException
import com.boclips.orders.domain.exceptions.MissingCurrencyForChannel
import com.boclips.orders.domain.exceptions.MissingVideoFullProjectionLink
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.httpclient.test.fakes.FakeClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.PriceResource
import com.boclips.videos.api.response.video.VideoResource
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import java.lang.RuntimeException
import java.math.BigDecimal
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
                price = PriceResource(amount = BigDecimal(600), currency = Currency.getInstance("USD")),
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
    fun `VideoNotFoundException if video cannot be found`() {
        assertThrows<VideoNotFoundException> {
            videoProvider.get(videoId = VideoId(value = "hideandseek"))
        }
    }

    @Test
    fun `FetchVideoResourceException when there are issues when fetching video (other than not found)`() {
        val videosClientMock = mock<VideosClient>()
        `when`(videosClientMock.getVideo("id", Projection.full)).thenThrow(
            FakeClient.conflictException("random feign exception appears!")
        )

        assertThrows<FetchVideoResourceException> {
            VideoServiceVideoProvider(videosClientMock, fakeChannelsClient).get(videoId = VideoId("id"))
        }
    }

    @Test
    fun `FetchVideoResourceException when there are internal issues when fetching video`() {
        val videosClientMock = mock<VideosClient>()
        `when`(videosClientMock.getVideo("id", Projection.full)).thenThrow(RuntimeException("¡no one expects the spanish inquisition!"))

        assertThrows<FetchVideoResourceException> {
            VideoServiceVideoProvider(videosClientMock, fakeChannelsClient).get(videoId = VideoId("id"))
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
