package com.boclips.terry.infrastructure.orders.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails
import org.springframework.http.HttpStatus

open class UserFacingException(
    override val message: String,
    status: HttpStatus = HttpStatus.BAD_REQUEST,
    error: String = status.reasonPhrase
): BoclipsApiException(ExceptionDetails(
    error = error,
    message = message,
    status = status
))