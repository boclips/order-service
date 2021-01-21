package com.boclips.orders.presentation

data class PlaceOrderRequest(
    val items: Set<PlaceOrderRequestItem>,
    val note: String?,
    val user: PlaceOrderRequestUser
)

data class PlaceOrderRequestItem(
    val id: String,
    val videoId: String,
    val additionalServices: AdditionalServicesRequest?
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
