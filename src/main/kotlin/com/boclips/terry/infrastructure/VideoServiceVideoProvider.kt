package com.boclips.terry.infrastructure

import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.videos.service.client.VideoServiceClient
import org.springframework.stereotype.Component

@Component
class VideoServiceVideoProvider(private val videoServiceClient: VideoServiceClient) : VideoProvider {
    override fun get(videoId: VideoId): Video? {
        try {
            val videoResource = videoServiceClient.rawIdToVideoId(videoId.value).let {
                videoServiceClient.get(it)
            }

            return Video(
                videoServiceId = VideoId(value = videoResource.videoId.value),
                title = videoResource.title,
                type = videoResource.type.toString(),
                videoReference = videoResource.contentPartnerVideoId,
                contentPartner = ContentPartner(
                    videoServiceId = ContentPartnerId(value = videoResource.contentPartnerId),
                    name = videoResource.createdBy
                )
            )
        } catch (e: Exception) {
            return null
        }
    }
}
