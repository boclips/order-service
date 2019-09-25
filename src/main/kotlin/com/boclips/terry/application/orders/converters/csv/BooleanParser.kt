package com.boclips.terry.application.orders.converters.csv

fun String?.parseBoolean() = this?.toLowerCase() == "yes"
