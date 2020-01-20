package com.boclips.orders.domain.model.orderItem

import java.time.temporal.ChronoUnit

sealed class Duration {
    abstract fun toReadableString(): String
    data class Time(val amount: Int, val unit: ChronoUnit) : Duration() {
        override fun toReadableString() = "$amount"
    }

    data class Description(val label: String) : Duration() {
        override fun toReadableString() = label
    }
}

