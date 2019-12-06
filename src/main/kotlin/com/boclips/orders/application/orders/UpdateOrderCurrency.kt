package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.service.OrderService
import org.springframework.stereotype.Component
import java.util.Currency

@Component
class UpdateOrderCurrency(
    private val orderService: OrderService
) {
    operator fun invoke(orderId: String, currency: String): Order {
        validateCurrency(currency)
        return orderService.updateCurrency(OrderId(orderId), Currency.getInstance(currency))
    }

    private fun validateCurrency(currency: String) {
        (Currency.getAvailableCurrencies()
            .firstOrNull { it.currencyCode == currency }
            ?: throw InvalidCurrencyFormatException(currency))
    }
}
