package com.boclips.orders.domain.exceptions

abstract class BoclipsException(override val message: String) : RuntimeException(message)
