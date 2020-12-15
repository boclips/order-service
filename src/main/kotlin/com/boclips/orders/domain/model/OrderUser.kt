package com.boclips.orders.domain.model

sealed class OrderUser {
    data class CompleteUser(
        val firstName: String,
        val lastName: String,
        val email: String,
        val legacyUserId: String? = null,
        val userId: String? = null
    ) : OrderUser()

    data class BasicUser(val label: String) : OrderUser()
}
