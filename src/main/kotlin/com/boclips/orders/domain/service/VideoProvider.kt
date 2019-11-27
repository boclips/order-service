package com.boclips.orders.domain.service

import com.boclips.orders.domain.model.orderItem.Video
import com.boclips.orders.domain.model.orderItem.VideoId

interface VideoProvider {
    fun get(videoId: VideoId): Video
}
