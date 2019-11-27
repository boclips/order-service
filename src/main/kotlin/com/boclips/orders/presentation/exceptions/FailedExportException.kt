package com.boclips.orders.presentation.exceptions

import com.boclips.web.exceptions.BoclipsApiException
import com.boclips.web.exceptions.ExceptionDetails

class FailedExportException(error: String, message: String) : BoclipsApiException(
    ExceptionDetails(
        error = error,
        message = message
    )
)
