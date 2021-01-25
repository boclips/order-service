package com.boclips.orders.presentation

import com.boclips.orders.presentation.carts.TrimServiceRequest

data class PlaceOrderRequest(
    val items: Set<PlaceOrderRequestItem>,
    val note: String?,
    val user: PlaceOrderRequestUser
)

data class PlaceOrderRequestItem(
    val id: String,
    val videoId: String,
    val additionalServices: PlaceOrderAdditionalServices?
)

data class PlaceOrderRequestUser(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val organisation: PlaceOrderRequestOrganisation
)

data class PlaceOrderRequestOrganisation(
    val id: String?,
    val name: String
)

data class PlaceOrderAdditionalServices(
    val trim: TrimServiceRequest? = null,
    val transcriptRequested: Boolean? = null,
    val editingRequested: String? = null
)
