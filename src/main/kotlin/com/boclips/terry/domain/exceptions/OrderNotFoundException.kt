package com.boclips.terry.domain.exceptions

import com.boclips.terry.domain.model.OrderId

class OrderNotFoundException(orderId: OrderId) : BoclipsException("Could not find order with ID=${orderId.value}")
