package com.boclips.orders.domain.model.cart

import com.boclips.orders.domain.model.video.VideoId

data class CartItem(
    val id: String,
    val videoId: VideoId,
    val additionalServices: AdditionalServices? = null
)

data class AdditionalServices(
    val trim: TrimService
)

data class TrimService(
    val trim: Boolean,
    val from: String,
    val to: String
)
