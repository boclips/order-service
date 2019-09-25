package com.boclips.terry.application.orders.converters.csv

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun String?.parseCsvDate(default: String? = null): Instant? =
    this?.takeIf { it.matches(Regex("^([0-2][0-9]|(3)[0-1])(/)(((0)[0-9])|((1)[0-2]))(/)\\d{4}$")) }?.let {
        LocalDate
            .parse(this, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
    } ?: default?.parseCsvDate()
