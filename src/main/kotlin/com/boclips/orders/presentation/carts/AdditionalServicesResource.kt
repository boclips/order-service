package com.boclips.orders.presentation.carts

data class AdditionalServicesResource(
    val trim: TrimServiceResource?
)

data class TrimServiceResource(
    val from: String,
    val to: String
)
