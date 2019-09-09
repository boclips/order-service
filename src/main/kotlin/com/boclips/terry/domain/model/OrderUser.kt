package com.boclips.terry.domain.model

sealed class OrderUser {
    data class CompleteUser(
        val firstName: String,
        val lastName: String,
        val email: String,
        val legacyUserId: String,
        val organisation: OrderOrganisation
    ) : OrderUser()

    data class BasicUser(val label: String) : OrderUser()
}