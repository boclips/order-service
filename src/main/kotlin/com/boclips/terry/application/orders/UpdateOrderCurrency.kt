package com.boclips.terry.application.orders

import com.boclips.terry.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.service.OrderService
import org.springframework.stereotype.Component
import java.util.Currency

@Component
class UpdateOrderCurrency(private val orderService: OrderService) {
    operator fun invoke(orderId: String, currency: String): Order {
        validateCurrency(currency)

        return orderService.update(
            OrderUpdateCommand.UpdateOrderItemsCurrency(OrderId(value = orderId), Currency.getInstance(currency))
        )
    }

    private fun validateCurrency(currency: String) {
        (Currency.getAvailableCurrencies()
            .firstOrNull { it.currencyCode == currency }
            ?: throw InvalidCurrencyFormatException(currency))
    }
}
