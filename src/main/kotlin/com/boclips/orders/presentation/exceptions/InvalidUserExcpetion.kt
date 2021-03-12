package com.boclips.orders.presentation.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

class InvalidUserException : BoclipsApiException(
    exceptionDetails = ExceptionDetails(
        error = HttpStatus.FORBIDDEN.reasonPhrase,
        message = "Cannot place orders for another user or organisation",
        status = HttpStatus.FORBIDDEN
    )
) {
}
