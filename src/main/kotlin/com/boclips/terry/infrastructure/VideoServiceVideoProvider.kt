package com.boclips.terry.infrastructure

import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.terry.infrastructure.orders.exceptions.MissingCurrencyForContentPartner
import com.boclips.videos.service.client.ContentPartnerId
import com.boclips.videos.service.client.VideoServiceClient
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class VideoServiceVideoProvider(private val videoServiceClient: VideoServiceClient) : VideoProvider {
    companion object : KLogging()

    override fun get(videoId: VideoId): Video? {
        try {
            val videoResource = videoServiceClient.rawIdToVideoId(videoId.value).let {
                videoServiceClient.get(it)
            }

            val contentPartner = videoServiceClient.findContentPartner(ContentPartnerId(videoResource.contentPartnerId))
            return Video(
                videoServiceId = VideoId(value = videoResource.videoId.value),
                title = videoResource.title,
                type = videoResource.type.toString(),
                videoReference = videoResource.contentPartnerVideoId,
                contentPartner = ContentPartner(
                    videoServiceId = com.boclips.terry.domain.model.orderItem.ContentPartnerId(value = videoResource.contentPartnerId),
                    name = videoResource.createdBy,
                    currency = contentPartner.currency
                        ?: throw MissingCurrencyForContentPartner(contentPartnerName = contentPartner.name)
                )
            )
        } catch (e: MissingCurrencyForContentPartner) {
            throw e
        } catch (e: Exception) {
            logger.info("Could not fetch video because:", e)
            return null
        }
    }
}
