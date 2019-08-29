package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.orderItem.Territory
import java.time.temporal.ChronoUnit

data class LicenseDocument (
    val amount: Int,
    val unit: ChronoUnit,
    val territory: Territory
)
