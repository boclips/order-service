package com.boclips.orders.presentation.carts

data class AdditionalServicesResource(
    val trim: TrimServiceResource?,
    val transcriptRequested: Boolean,
    val captionsRequested: Boolean,
    val editingRequested: String?
)

data class TrimServiceResource(
    val from: String,
    val to: String
)
