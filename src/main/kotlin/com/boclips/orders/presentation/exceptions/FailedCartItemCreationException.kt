package com.boclips.orders.presentation.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class FailedCartItemCreationException(error: String, message: String, status: HttpStatus) : BoclipsApiException(
    ExceptionDetails(
        error = error,
        message = message,
        status = status
    )
)
