package com.boclips.terry.infrastructure.orders

import java.time.temporal.ChronoUnit

data class LicenseDocument (
    val amount: Int,
    val unit: ChronoUnit,
    val territory: String
)
