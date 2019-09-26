package com.boclips.terry.presentation

import com.boclips.terry.domain.exceptions.BoclipsException
import com.boclips.terry.domain.exceptions.OrderItemNotFoundException
import com.boclips.terry.domain.exceptions.OrderNotFoundException
import com.boclips.web.ExceptionHandlingControllerAdvice
import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class BoclipsExceptionHandler(val exceptionHandlingControllerAdvice: ExceptionHandlingControllerAdvice) {

    @ExceptionHandler
    fun handleBoclipsException(e: BoclipsException, webRequest: WebRequest): ResponseEntity<*> {
        return when (e) {
            is OrderNotFoundException,
            is OrderItemNotFoundException -> exceptionHandlingControllerAdvice.handleBoclipsApiExceptions(
                OrderServiceApiException(e.message, HttpStatus.NOT_FOUND),
                webRequest
            )
            else -> exceptionHandlingControllerAdvice.handleBoclipsApiExceptions(
                OrderServiceApiException(e.message),
                webRequest
            )
        }
    }
}

class OrderServiceApiException(message: String, status: HttpStatus = HttpStatus.BAD_REQUEST) : BoclipsApiException(
    ExceptionDetails(
        error = status.reasonPhrase,
        message = message,
        status = status
    )
)
