package com.boclips.terry.infrastructure.orders.exceptions

import com.boclips.terry.domain.model.OrderId

class OrderNotFoundException(orderId: OrderId) : UserFacingException("Could not find order: $orderId")
