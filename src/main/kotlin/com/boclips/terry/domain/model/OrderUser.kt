package com.boclips.terry.domain.model

data class OrderUser(
    val firstName: String,
    val lastName: String,
    val email: String,
    val sourceUserId: String,
    val organisation: OrderOrganisation
)
