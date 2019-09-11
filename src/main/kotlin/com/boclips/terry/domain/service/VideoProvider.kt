package com.boclips.terry.domain.service

import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId

interface VideoProvider {
    fun get(videoId: VideoId): Video?
}
