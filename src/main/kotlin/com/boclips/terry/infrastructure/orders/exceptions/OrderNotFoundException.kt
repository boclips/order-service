package com.boclips.terry.infrastructure.orders.exceptions

import com.boclips.terry.domain.model.OrderId

class OrderNotFoundException(orderId: OrderId) : RuntimeException("Could not find order: $orderId")
