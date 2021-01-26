package com.boclips.orders.infrastructure

import com.boclips.orders.domain.FetchVideoResourceException
import com.boclips.orders.domain.exceptions.ChannelNotFoundException
import com.boclips.orders.domain.exceptions.MissingCurrencyForChannel
import com.boclips.orders.domain.exceptions.MissingVideoFullProjectionLink
import com.boclips.orders.domain.exceptions.MissingVideoPlaybackId
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Channel
import com.boclips.orders.domain.model.orderItem.ChannelId
import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.orders.infrastructure.orders.converters.KalturaLinkConverter
import com.boclips.videos.api.httpclient.ChannelsClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.request.Projection
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.VideoResource
import feign.FeignException
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
            videoServiceId = VideoId(
                value = videoResource.id
                    ?: throw IllegalStateException("Missing video for id $videoId")
            ),
            title = videoResource.title ?: throw IllegalStateException("Missing title for video $videoId"),
            types = videoResource.types?.map { it.name } ?: emptyList(),
            channelVideoId = videoResource.channelVideoId
                ?: throw IllegalStateException("Missing channel video id for video $videoId"),
            channel = Channel(
                videoServiceId = ChannelId(
                    value = videoResource.channelId
                        ?: throw IllegalStateException("Missing channel id for video $videoId")
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
                CaptionStatus.HUMAN_GENERATED_AVAILABLE -> AssetStatus.AVAILABLE
                CaptionStatus.AUTO_GENERATED_AVAILABLE -> AssetStatus.UNAVAILABLE
                CaptionStatus.NOT_AVAILABLE -> AssetStatus.UNAVAILABLE
                CaptionStatus.UNKNOWN -> AssetStatus.UNKNOWN
                null -> AssetStatus.UNKNOWN
            },
            downloadableVideoStatus = when ((videoResource.playback as? StreamPlaybackResource)?.maxResolutionAvailable) {
                null -> AssetStatus.UNKNOWN
                true -> AssetStatus.AVAILABLE
                false -> AssetStatus.UNAVAILABLE
            },
            captionAdminLink = KalturaLinkConverter.getCaptionAdminLink(videoResource.playback?.id),
            videoUploadLink = KalturaLinkConverter.getVideoUploadLink(videoResource.playback?.id),
            playbackId = videoResource.playback?.id ?: throw MissingVideoPlaybackId(videoId),
            price = videoResource.price?.let { Price(amount = it.amount, currency = it.currency) } ?: throw TODO("no price on video?")
        )
    }

    private fun getChannel(videoResource: VideoResource): ChannelResource {
        return try {
            videoResource.channelId?.let { channelsClient.getChannel(it) }
                ?: throw java.lang.IllegalStateException("Missing channelId on videoResource")
        } catch (e: FeignException) {
            when (e.status()) {
                404 -> throw ChannelNotFoundException(ChannelId(videoResource.channelId ?: ""))
                else ->
                    throw e
                        .also { logger.warn(e) { "Something went wrong when fetching channel: ${videoResource.channel}" } }
            }
        }
    }

    private fun getVideoResource(videoId: VideoId): VideoResource {
        return try {
            videosClient.getVideo(videoId = videoId.value, projection = Projection.full)
        } catch (e: FeignException) {
            logger.info { "FeignException exception with status code: ${e.status()}, message: ${e.message}" }
            throw when (e) {
                is FeignException.NotFound -> VideoNotFoundException(videoId, e)
                else -> FetchVideoResourceException(videoId, e)
            }
        } catch (e: Exception) {
            logger.info { e.message }
            throw FetchVideoResourceException(videoId, e)
        }
    }
}
