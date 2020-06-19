package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.model.orderItem.AssetStatus
import java.net.URL

class VideoDocument(
    val videoServiceId: String,
    val title: String,
    val type: String,
    val fullProjectionLink: String,
    val playbackId: String?,
    val captionStatus: String?,
    val hasHDVideo: Boolean?
)
