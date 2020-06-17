package com.boclips.orders.infrastructure

import com.boclips.orders.domain.exceptions.ChannelNotFoundException
import com.boclips.orders.domain.exceptions.MissingCurrencyForChannel
import com.boclips.orders.domain.exceptions.MissingVideoFullProjectionLink
import com.boclips.orders.domain.exceptions.MissingVideoPlaybackId
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Channel
import com.boclips.orders.domain.model.orderItem.ChannelId
import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import mu.KLogging
import org.springframework.stereotype.Component
import java.net.URL
import java.util.Currency

@Component
class VideoServiceVideoProvider(
    private val videosClient: VideosClient,
    private val channelsClient: ChannelsClient
) : VideoProvider {
    companion object : KLogging()

    override fun get(videoId: VideoId): Video {
        val videoResource = getVideoResource(videoId)
        val channel = getChannel(videoResource)

        return Video(
            videoServiceId = VideoId(value = videoResource.id ?: throw IllegalStateException("Missing video for id $videoId")),
            title = videoResource.title ?: throw IllegalStateException("Missing title for video $videoId"),
            type = videoResource.type?.name.toString(),
            channelVideoId = videoResource.channelVideoId ?: throw IllegalStateException("Missing content partner video id for video $videoId"),
            channel = Channel(
                videoServiceId = ChannelId(value = videoResource.channelId
                    ?: throw IllegalStateException("Missing content partner id for video $videoId")
                ),
                name = videoResource.createdBy
                    ?: throw IllegalStateException("Missing 'created by' for video $videoId"),
                currency = channel.currency
                    ?.let { Currency.getInstance(channel.currency) }
                    ?: throw MissingCurrencyForChannel(channel = channel.name)

            ),
            fullProjectionLink = URL(
                videoResource._links?.get("fullProjection")?.href ?: throw MissingVideoFullProjectionLink(videoId)
            ),
            captionStatus = when (videoResource.captionStatus) {
                CaptionStatus.REQUESTED -> AssetStatus.REQUESTED
                CaptionStatus.PROCESSING -> AssetStatus.PROCESSING
                CaptionStatus.AVAILABLE -> AssetStatus.AVAILABLE
                CaptionStatus.NOT_AVAILABLE -> AssetStatus.UNAVAILABLE
                CaptionStatus.UNKNOWN -> AssetStatus.UNKNOWN
                null -> AssetStatus.UNKNOWN
            },
            downloadableVideoStatus = when ((videoResource.playback as? StreamPlaybackResource)?.maxResolutionAvailable) {
                null -> AssetStatus.UNKNOWN
                true -> AssetStatus.AVAILABLE
                false -> AssetStatus.UNAVAILABLE
            },
            captionAdminLink = URL("https://kmc.kaltura.com/index.php/kmcng/content/entries/entry/${videoResource.playback?.id}/metadata"),
            videoUploadLink = URL("https://kmc.kaltura.com/index.php/kmcng/content/entries/entry/${videoResource.playback?.id}/flavours"),
            playbackId = videoResource.playback?.id ?: throw MissingVideoPlaybackId(videoId)
        )
    }

    private fun getChannel(videoResource: VideoResource): ChannelResource {
        return try {
            channelsClient.getChannel(videoResource.channelId!!)
        } catch (e: Exception) {
            throw ChannelNotFoundException(ChannelId(videoResource.channelId ?: ""))
        }
    }

    private fun getVideoResource(videoId: VideoId): VideoResource {
        return try {
            videosClient.getVideo(videoId = videoId.value, projection = Projection.full)
        } catch (e: Exception) {
            throw VideoNotFoundException(videoId, e)
        }
    }
}
