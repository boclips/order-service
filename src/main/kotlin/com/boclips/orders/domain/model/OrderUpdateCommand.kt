package com.boclips.orders.domain.model

import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.Video
import java.math.BigDecimal
import java.util.*

sealed class OrderUpdateCommand(val orderId: OrderId) {
    class ReplaceStatus(orderId: OrderId, val orderStatus: OrderStatus) : OrderUpdateCommand(orderId)
    class UpdateOrderCurrency(orderId: OrderId, val currency: Currency, val fxRateToGbp: BigDecimal) :
        OrderUpdateCommand(orderId)

    sealed class OrderItemUpdateCommand(orderId: OrderId, val orderItemsId: String) : OrderUpdateCommand(orderId) {
        class UpdateOrderItemPrice(orderId: OrderId, orderItemsId: String, val amount: BigDecimal) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class UpdateOrderItemLicense(
            orderId: OrderId,
            orderItemsId: String,
            val orderItemLicense: OrderItemLicense
        ) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class ReplaceVideo(orderId: OrderId, orderItemsId: String, val video: Video) :
            OrderItemUpdateCommand(orderId, orderItemsId)
    }
}
