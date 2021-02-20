package com.boclips.orders.domain.service

import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.video.VideoId

interface VideoProvider {
    fun get(videoId: VideoId, userId: String): Video
}
