package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidUpdateOrderItemRequest
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.presentation.UpdateOrderItemRequest
import org.springframework.stereotype.Component

@Component
class UpdateOrderItem(private val orderService: OrderService) {
    operator fun invoke(id: String, orderItemId: String, updateRequest: UpdateOrderItemRequest?) {
        val orderId = OrderId(value = id)
        val priceUpdate = updateRequest?.price?.let {
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice(
                orderId = orderId,
                orderItemsId = orderItemId,
                amount = it
            )
        }

        val territoryUpdate = updateRequest?.license?.territory?.let {
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemTerritory(
                orderId = orderId,
                orderItemsId = orderItemId,
                territory = it
            )
        }

        val durationUpdate = updateRequest?.license?.duration?.let {
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemDuration(
                orderId = orderId,
                orderItemsId = orderItemId,
                duration = it
            )
        }

        val commands = listOfNotNull(priceUpdate, durationUpdate, territoryUpdate)

        if (commands.isEmpty()) {
            throw InvalidUpdateOrderItemRequest()
        }

        orderService.bulkUpdate(commands)
    }
}
