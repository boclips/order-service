package com.boclips.terry.application.exceptions

import com.boclips.terry.domain.model.OrderId
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class OrderNotFoundException(orderId: OrderId) : RuntimeException("Cannot find order for id: ${orderId.value}")
