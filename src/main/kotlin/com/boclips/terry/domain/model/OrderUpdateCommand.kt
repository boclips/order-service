package com.boclips.terry.domain.model

import java.util.Currency

sealed class OrderUpdateCommand(val orderId: OrderId) {
    class ReplaceStatus(orderId: OrderId, val orderStatus: OrderStatus) : OrderUpdateCommand(orderId)
    class UpdateOrderItemsCurrency(orderId: OrderId, val currency: Currency) : OrderUpdateCommand(orderId)
}
