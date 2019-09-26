package com.boclips.terry.domain.exceptions

import com.boclips.terry.domain.model.OrderId

class OrderItemNotFoundException(orderId: OrderId, orderItemId: String) :
    BoclipsException("Could not find order-item $orderItemId for order: ${orderId.value}")
