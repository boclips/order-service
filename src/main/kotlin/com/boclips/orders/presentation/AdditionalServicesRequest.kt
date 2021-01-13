package com.boclips.orders.presentation

data class AdditionalServicesRequest(
    val trim: TrimServiceRequest
)

data class TrimServiceRequest(
    val trim: Boolean,
    val from: String,
    val to: String
)
