package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.OrderId

class OrderNotFoundException(orderId: OrderId) : BoclipsException("Could not find order with ID=${orderId.value}")
