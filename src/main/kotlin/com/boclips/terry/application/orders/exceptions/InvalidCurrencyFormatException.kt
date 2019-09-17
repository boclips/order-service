package com.boclips.terry.application.orders.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class InvalidCurrencyFormatException(currency: String): RuntimeException("$currency is not a valid ISO-4217 currency format")
