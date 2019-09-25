package com.boclips.terry.application.orders.exceptions

abstract class InvalidCsvParsingException(override val message: String) : RuntimeException(message)
