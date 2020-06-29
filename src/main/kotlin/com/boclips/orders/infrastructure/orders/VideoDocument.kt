package com.boclips.orders.infrastructure.orders

class VideoDocument(
    val videoServiceId: String,
    val title: String,
    val type: String,
    val types: List<String>?,
    val fullProjectionLink: String,
    val playbackId: String,
    val captionStatus: String,
    val hasHDVideo: Boolean
)
