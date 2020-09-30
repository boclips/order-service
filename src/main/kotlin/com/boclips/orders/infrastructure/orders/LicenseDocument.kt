package com.boclips.orders.infrastructure.orders

import java.time.temporal.ChronoUnit

data class LicenseDocument(
    val amount: Int?,
    val unit: ChronoUnit?,
    val description: String?,
    val territory: String?
) {
    fun isValidTime(): Boolean {
        return amount != null && unit != null
    }

    fun isValidDescription(): Boolean {
        return description != null
    }
}
