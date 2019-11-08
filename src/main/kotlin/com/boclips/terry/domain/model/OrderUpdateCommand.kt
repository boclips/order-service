package com.boclips.terry.domain.model

import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import java.math.BigDecimal
import java.util.Currency

sealed class OrderUpdateCommand(val orderId: OrderId) {
    class ReplaceStatus(orderId: OrderId, val orderStatus: OrderStatus) : OrderUpdateCommand(orderId)
    class UpdateOrderItemsCurrency(orderId: OrderId, val currency: Currency) : OrderUpdateCommand(orderId)

    sealed class OrderItemUpdateCommand(orderId: OrderId, val orderItemsId: String) : OrderUpdateCommand(orderId) {
        class UpdateOrderItemPrice(orderId: OrderId, orderItemsId: String, val amount: BigDecimal) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class UpdateOrderItemLicense(
            orderId: OrderId,
            orderItemsId: String,
            val orderItemLicense: OrderItemLicense
        ) :
            OrderItemUpdateCommand(orderId, orderItemsId)
    }
}
