package com.boclips.orders.domain.exceptions

import com.boclips.orders.domain.model.OrderId

class OrderItemNotFoundException(orderId: OrderId, orderItemId: String) :
    BoclipsException("Could not find order-item $orderItemId for order: ${orderId.value}")
