package com.boclips.orders.infrastructure.carts

data class CartItemDocument(
    val id: String,
    val videoId: String,
    val additionalServices: AdditionalServicesDocument? = null
)

data class AdditionalServicesDocument(
    val trim: TrimServiceDocument?
)

data class TrimServiceDocument(
    val from: String,
    val to: String
)
