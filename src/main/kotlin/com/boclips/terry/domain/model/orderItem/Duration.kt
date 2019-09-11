package com.boclips.terry.domain.model.orderItem

import java.time.temporal.ChronoUnit

sealed class Duration {
    data class Time(val amount: Int, val unit: ChronoUnit) : Duration()
    data class Description(val label: String) : Duration()
}

