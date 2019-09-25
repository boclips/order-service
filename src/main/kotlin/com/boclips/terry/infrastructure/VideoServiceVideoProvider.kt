package com.boclips.terry.infrastructure

import com.boclips.terry.domain.exceptions.ContentPartnerNotFoundException
import com.boclips.terry.domain.exceptions.MissingCurrencyForContentPartner
import com.boclips.terry.domain.exceptions.VideoNotFoundException
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.videos.service.client.VideoServiceClient
import mu.KLogging
import org.springframework.stereotype.Component
import com.boclips.videos.service.client.ContentPartner as VideoClientContentPartner
import com.boclips.videos.service.client.ContentPartnerId as VideoClientContentPartnerId
import com.boclips.videos.service.client.Video as VideoClientVideo

@Component
class VideoServiceVideoProvider(private val videoServiceClient: VideoServiceClient) : VideoProvider {
    companion object : KLogging()

    override fun get(videoId: VideoId): Video {
        val videoResource = getVideoResource(videoId)
        val contentPartner = getContentPartner(videoResource)

        return Video(
            videoServiceId = VideoId(value = videoResource.videoId.value),
            title = videoResource.title,
            type = videoResource.type.toString(),
            videoReference = videoResource.contentPartnerVideoId,
            contentPartner = ContentPartner(
                videoServiceId = ContentPartnerId(value = videoResource.contentPartnerId),
                name = videoResource.createdBy,
                currency = contentPartner.currency
                    ?: throw MissingCurrencyForContentPartner(contentPartnerName = contentPartner.name)
            )
        )
    }

    private fun getContentPartner(videoResource: VideoClientVideo): VideoClientContentPartner {
        val contentPartner = try {
            videoServiceClient.findContentPartner(VideoClientContentPartnerId(videoResource.contentPartnerId))
        } catch (e: Exception) {
            throw ContentPartnerNotFoundException(ContentPartnerId(videoResource.contentPartnerId))
        }
        return contentPartner ?: throw ContentPartnerNotFoundException(
            ContentPartnerId(videoResource.contentPartnerId)
        )
    }

    private fun getVideoResource(videoId: VideoId): com.boclips.videos.service.client.Video {
        val videoResource = try {
            videoServiceClient.rawIdToVideoId(videoId.value).let {
                videoServiceClient.get(it)
            }
        } catch (e: Exception) {
            throw VideoNotFoundException(videoId)
        }
        return videoResource ?: throw VideoNotFoundException(videoId)
    }
}
