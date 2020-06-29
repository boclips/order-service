package com.boclips.orders.domain.model.orderItem

import java.net.URL

data class Video(
    val videoServiceId: VideoId,
    val playbackId: String,
    val title: String,
    val types: List<String>,
    val channelVideoId: String,
    val channel: Channel,
    val fullProjectionLink: URL,
    val captionStatus: AssetStatus,
    val downloadableVideoStatus: AssetStatus,
    val videoUploadLink: URL,
    val captionAdminLink: URL
)

enum class AssetStatus {
    AVAILABLE, UNAVAILABLE, REQUESTED, PROCESSING, UNKNOWN
}
