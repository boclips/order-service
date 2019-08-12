package com.boclips.terry.domain.model

import com.boclips.videos.service.client.VideoType
import java.math.BigDecimal

data class OrderItem(
    val uuid: String,
    val price: BigDecimal,
    val transcriptRequested: Boolean,
    val video: Video
)
