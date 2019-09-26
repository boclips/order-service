package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class UpdateOrderItemPrice(private val ordersRepository: OrdersRepository) {
    operator fun invoke(orderId: String, orderItemId: String, amount: BigDecimal): Order {
        return ordersRepository.update(
            OrderUpdateCommand.UpdateOrderItemPrice(
                orderId = OrderId(value = orderId),
                orderItemsId = orderItemId,
                amount = amount
            )
        )
    }
}
