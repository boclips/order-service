package com.boclips.orders.infrastructure.users

data class UserResource (
    val userId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val legacyUserId: String? = null,
    val organisationId: String? = null,
    val organisationName: String? = null
)
