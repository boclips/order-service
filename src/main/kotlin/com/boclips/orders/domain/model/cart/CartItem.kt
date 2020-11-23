package com.boclips.orders.domain.model.cart

import com.boclips.orders.domain.model.video.VideoId

data class CartItem(
    val id: String,
    val videoId: VideoId
)