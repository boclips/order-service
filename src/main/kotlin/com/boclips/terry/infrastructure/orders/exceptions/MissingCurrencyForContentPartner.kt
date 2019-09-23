package com.boclips.terry.infrastructure.orders.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.BAD_REQUEST)
class MissingCurrencyForContentPartner(contentPartnerName: String) :
    RuntimeException("Content partner '$contentPartnerName' has no currency defined") {
}
