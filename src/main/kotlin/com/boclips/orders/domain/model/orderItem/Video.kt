package com.boclips.orders.domain.model.orderItem

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.video.VideoId
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
    val captionAdminLink: URL,
    val price: Price?
)

enum class AssetStatus {
    AVAILABLE, UNAVAILABLE, REQUESTED, PROCESSING, UNKNOWN
}
