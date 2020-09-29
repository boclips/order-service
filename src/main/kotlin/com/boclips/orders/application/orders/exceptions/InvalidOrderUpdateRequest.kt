package com.boclips.orders.application.orders.exceptions

import java.lang.RuntimeException

class InvalidOrderUpdateRequest(message: String) : RuntimeException(message)
