package com.boclips.terry.application.exceptions

import com.boclips.terry.application.orders.converters.csv.Errors
import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidCsvException(errors: Errors): BoclipsApiException(ExceptionDetails(
    error = "Invalid CSV",
    message = errors.errors.map { "Order ${it.legacyOrderId}: ${it.message}" }.joinToString("\n"),
    status = HttpStatus.BAD_REQUEST
))
