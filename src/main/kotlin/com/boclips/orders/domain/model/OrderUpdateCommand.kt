package com.boclips.orders.domain.model

import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Video
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

sealed class OrderUpdateCommand(val orderId: OrderId) {
    class ReplaceStatus(orderId: OrderId, val orderStatus: OrderStatus) : OrderUpdateCommand(orderId)

    class ReplaceDeliveredAt(orderId: OrderId, val deliveredAt: Instant?) : OrderUpdateCommand(orderId)

    class UpdateOrderCurrency(orderId: OrderId, val currency: Currency, val fxRateToGbp: BigDecimal) :
        OrderUpdateCommand(orderId)

    class UpdateOrderOrganisation(orderId: OrderId, val organisation: OrderOrganisation) : OrderUpdateCommand(orderId)

    sealed class OrderItemUpdateCommand(orderId: OrderId, val orderItemsId: String) : OrderUpdateCommand(orderId) {
        class UpdateOrderItemPrice(orderId: OrderId, orderItemsId: String, val amount: BigDecimal) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class UpdateOrderItemDuration(orderId: OrderId, orderItemsId: String, val duration: String) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class UpdateOrderItemTerritory(orderId: OrderId, orderItemsId: String, val territory: String) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class ReplaceVideo(orderId: OrderId, orderItemsId: String, val video: Video) :
            OrderItemUpdateCommand(orderId, orderItemsId)

        class UpdateCaptionStatus(orderId: OrderId, orderItemId: String, val captionStatus: AssetStatus) :
            OrderItemUpdateCommand(orderId, orderItemsId = orderItemId)
    }
}
