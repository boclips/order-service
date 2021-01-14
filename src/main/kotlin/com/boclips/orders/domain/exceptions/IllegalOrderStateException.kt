package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.OrderId

class IllegalOrderStateException(orderId: OrderId, message: String) :
    BoclipsException("Illegal state of order with ID=${orderId.value}: $message")
