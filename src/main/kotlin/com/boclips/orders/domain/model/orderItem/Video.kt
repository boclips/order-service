package com.boclips.orders.domain.model.orderItem

import java.net.URL

data class Video(
    val videoServiceId: VideoId,
    val title: String,
    val type: String,
    val channelVideoId: String,
    val channel: Channel,
    val fullProjectionLink: URL
)
