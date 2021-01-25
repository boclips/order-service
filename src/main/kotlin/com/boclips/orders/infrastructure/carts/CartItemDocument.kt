package com.boclips.orders.infrastructure.carts

data class CartItemDocument(
    val id: String,
    val videoId: String,
    val additionalServices: AdditionalServicesDocument = AdditionalServicesDocument()
)

data class AdditionalServicesDocument(
    val trim: TrimServiceDocument? = null,
    val transcriptRequested: Boolean? = false,
    val captionsRequested: Boolean? = false,
    val editingRequested: String? = null
)

data class TrimServiceDocument(
    val from: String,
    val to: String
)
