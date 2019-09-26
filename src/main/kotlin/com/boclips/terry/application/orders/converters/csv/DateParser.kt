package com.boclips.terry.application.orders.converters.csv

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val ddMMyyyyRegex = Regex("^([0-2][0-9]|(3)[0-1])(/)(((0)[0-9])|((1)[0-2]))(/)\\d{4}$")
private val ddMMyyRegex = Regex("^([0-2][0-9]|(3)[0-1])(/)(((0)[0-9])|((1)[0-2]))(/)\\d{2}$")

fun String?.parseCsvDate(default: String? = null): Instant? {
    return when {
        this == null -> default?.parseCsvDate()
        this.matches(ddMMyyyyRegex) -> toInstant("dd/MM/yyyy")
        this.matches(ddMMyyRegex) -> toInstant("dd/MM/yy")
        else -> default?.parseCsvDate()
    }
}

private fun String?.toInstant(datePattern: String): Instant? {
    return LocalDate
        .parse(this, DateTimeFormatter.ofPattern(datePattern))
        .atStartOfDay()
        .toInstant(ZoneOffset.UTC)
}
