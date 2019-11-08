package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.service.OrderService
import com.boclips.terry.presentation.UpdateOrderItemRequest
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

        val licenseUpdate = updateRequest?.license?.let {
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemLicense(
                orderId = orderId,
                orderItemsId = orderItemId,
                orderItemLicense = OrderItemLicense(
                    territory = it.territory!!,
                    duration = Duration.Description(it.duration!!)
                )
            )
        }

        orderService.bulkUpdate(listOfNotNull(priceUpdate, licenseUpdate))
    }
}
