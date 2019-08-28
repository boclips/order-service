package com.boclips.terry.infrastructure.orders

data class OrderUserDocument (
    val firstName: String,
    val lastName: String,
    val email: String,
    val sourceUserId: String,
    val organisation: OrderOrganisationDocument
)
