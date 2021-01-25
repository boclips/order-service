package com.boclips.orders.domain.model.cart

import com.boclips.orders.domain.model.video.VideoId

data class CartItem(
    val id: String,
    val videoId: VideoId,
    val additionalServices: AdditionalServices = AdditionalServices()
)

data class AdditionalServices(
    val trim: TrimService? = null,
    val transcriptRequested: Boolean = false,
    val captionsRequested: Boolean = false,
    val editingRequested: String? = null
)

data class TrimService(
    val from: String,
    val to: String
)
