package com.boclips.terry.application.orders

import com.boclips.terry.application.exceptions.OrderNotFoundException
import com.boclips.terry.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component
import java.util.Currency

@Component
class UpdateOrderCurrency(private val ordersRepository: OrdersRepository) {
    operator fun invoke(orderId: String, currency: String): Order {
        validateCurrency(currency)

        return OrderId(orderId).let { id ->
            ordersRepository.findOne(id = id)
                ?.let {
                    ordersRepository.update(
                        OrderUpdateCommand.UpdateOrderItemsCurrency(it.id, Currency.getInstance(currency))
                    )
                }
                ?: throw OrderNotFoundException(orderId = id)

        }
    }

    private fun validateCurrency(currency: String) {
        (Currency.getAvailableCurrencies()
            .firstOrNull { it.currencyCode == currency }
            ?: throw InvalidCurrencyFormatException(currency))
    }
}
