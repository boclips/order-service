package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.presentation.UpdateOrderRequest
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.stereotype.Component

@Component
class UpdateOrder(
    private val orderService: OrderService
) {
    operator fun invoke(id: String, updateOrderRequest: UpdateOrderRequest?): OrderResource {
        updateOrderRequest?.organisation?.let {
            if (it.isBlank()) {
                throw InvalidOrderUpdateRequest("Organisation must not be blank")
            }

            val updateCommand = OrderUpdateCommand.UpdateOrderOrganisation(
                orderId = OrderId(id),
                organisation = OrderOrganisation(name = it)
            )

            val order = orderService.update(updateCommand)
            return OrderResource.fromOrder(order)
        } ?: throw InvalidOrderUpdateRequest("Update order request needs a valid organisation")
    }
}


