package com.boclips.terry.presentation

import com.boclips.terry.domain.exceptions.BoclipsException
import com.boclips.web.ExceptionHandlingControllerAdvice
import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class BoclipsExceptionHandler(val exceptionHandlingControllerAdvice: ExceptionHandlingControllerAdvice) {

    @ExceptionHandler
    fun handleBoclipsException(e: BoclipsException, webRequest: WebRequest) =
        exceptionHandlingControllerAdvice.handleBoclipsApiExceptions(
            OrderServiceApiException(e.message),
            webRequest
        )
}

class OrderServiceApiException(message: String) : BoclipsApiException(
    ExceptionDetails(
        error = HttpStatus.BAD_REQUEST.reasonPhrase,
        message = message,
        status = HttpStatus.BAD_REQUEST
    )
)
