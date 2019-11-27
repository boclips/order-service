package com.boclips.orders.application.orders.converters.csv

fun String?.parseBoolean() = this?.toLowerCase() == "yes"
