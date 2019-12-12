package com.boclips.orders.domain.exceptions

abstract class BoclipsException(override val message: String, cause: Throwable? = null) : RuntimeException(message, cause)
