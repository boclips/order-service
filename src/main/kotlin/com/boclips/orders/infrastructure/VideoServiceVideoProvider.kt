package com.boclips.orders.infrastructure

import com.boclips.orders.domain.exceptions.ContentPartnerNotFoundException
import com.boclips.orders.domain.exceptions.MissingCurrencyForContentPartner
import com.boclips.orders.domain.exceptions.MissingVideoFullProjectionLink
import com.boclips.orders.domain.exceptions.VideoNotFoundException
import com.boclips.orders.domain.model.orderItem.ContentPartner
import com.boclips.orders.domain.model.orderItem.ContentPartnerId
import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.videos.api.httpclient.ContentPartnersClient
import com.boclips.videos.api.httpclient.VideosClient
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.video.VideoResource
import mu.KLogging
import org.springframework.stereotype.Component
import java.net.URL
import java.util.Currency

@Component
class VideoServiceVideoProvider(
    private val videosClient: VideosClient,
    private val contentPartnersClient: ContentPartnersClient
) : VideoProvider {
    companion object : KLogging()

    override fun get(videoId: VideoId): Video {
        val videoResource = getVideoResource(videoId)
        val contentPartner = getContentPartner(videoResource)

        return Video(
            videoServiceId = VideoId(value = videoResource.id ?: throw IllegalStateException("Missing video for id $videoId")),
            title = videoResource.title ?: throw IllegalStateException("Missing title for video $videoId"),
            type = videoResource.type?.name.toString(),
            contentPartnerVideoId = videoResource.contentPartnerVideoId ?: throw IllegalStateException("Missing content partner video id for video $videoId"),
            contentPartner = ContentPartner(
                videoServiceId = ContentPartnerId(value = videoResource.contentPartnerId
                    ?: throw IllegalStateException("Missing content partner id for video $videoId")
                ),
                name = videoResource.createdBy ?: throw IllegalStateException("Missing 'created by' for video $videoId"),
                currency = contentPartner.currency
                    ?.let { Currency.getInstance(contentPartner.currency) }
                    ?: throw MissingCurrencyForContentPartner(contentPartnerName = contentPartner.name)

            ),
            fullProjectionLink = URL(videoResource._links?.get("fullProjection")?.href ?: throw MissingVideoFullProjectionLink(videoId))
        )
    }

    private fun getContentPartner(videoResource: VideoResource): ContentPartnerResource {
        return try {
            contentPartnersClient.getContentPartner(videoResource.contentPartnerId!!)
        } catch (e: Exception) {
            throw ContentPartnerNotFoundException(ContentPartnerId(videoResource.contentPartnerId ?: ""))
        }
    }

    private fun getVideoResource(videoId: VideoId): VideoResource {
        return try {
            videosClient.getVideo(videoId = videoId.value)
        } catch (e: Exception) {
            throw VideoNotFoundException(videoId, e)
        }
    }
}
