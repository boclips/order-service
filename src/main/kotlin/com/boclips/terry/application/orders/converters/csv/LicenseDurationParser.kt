package com.boclips.terry.application.orders.converters.csv

import com.boclips.terry.domain.model.orderItem.Duration
import java.time.temporal.ChronoUnit

fun String?.parseLicenseDuration(): Duration? {
    if (this.isNullOrBlank()) {
        return null
    }

    return this.toIntOrNull()?.let { duration ->
        Duration.Time(amount = duration, unit = ChronoUnit.YEARS)
    } ?: Duration.Description(label = this)
}

