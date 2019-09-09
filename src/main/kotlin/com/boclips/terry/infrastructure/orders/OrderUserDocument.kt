package com.boclips.terry.infrastructure.orders

data class OrderUserDocument(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val legacyUserId: String?,
    val organisation: OrderOrganisationDocument?,
    val label: String?
) {
    fun isBasicUser(): Boolean {
        return !label.isNullOrEmpty()
    }

    fun isCompleteUser(): Boolean {
        return !firstName.isNullOrEmpty() &&
            !lastName.isNullOrEmpty() &&
            !email.isNullOrEmpty() &&
            !legacyUserId.isNullOrEmpty() &&
            organisation != null
    }
}
