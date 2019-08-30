package com.boclips.terry.domain.model

data class OrderUser(
    val firstName: String,
    val lastName: String,
    val email: String,
    val legacyUserId: String,
    val organisation: OrderOrganisation
)
