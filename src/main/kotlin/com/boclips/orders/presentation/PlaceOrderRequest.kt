package com.boclips.orders.presentation

class PlaceOrderRequest(
    val items: Set<PlaceOrderRequestItem>,
    val user: PlaceOrderRequestUser
)

class PlaceOrderRequestItem(
    val id: String,
    val videoId: String,
    val additionalServices: AdditionalServicesRequest?
)

class PlaceOrderRequestUser(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val organisation: PlaceOrderRequestOrganisation
)

class PlaceOrderRequestOrganisation(
    val id: String?,
    val name: String
)
