package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory
import java.util.Currency

class UpdateOrderCurrencyIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var updateOrderCurrency: UpdateOrderCurrency

    @Test
    fun `updates the currency of an order`() {
        val order = ordersRepository.save(OrderFactory.orderInPounds())

        val updatedOrder = updateOrderCurrency(orderId = order.id.value, currency = "EUR")

        assertThat(updatedOrder.currency).isEqualTo(Currency.getInstance("EUR"))
        assertThat(updatedOrder.fxRateToGbp).isEqualTo("0.80000")
        assertThat(updatedOrder).isEqualToIgnoringGivenFields(order, "currency", "fxRateToGbp", "orderItems", "updatedAt", "status")
    }

    @Test
    fun `throws appropriately when order not found`() {
        assertThrows<OrderNotFoundException> {
            updateOrderCurrency(orderId = "whatever", currency = "EUR")
            updateOrderCurrency(orderId = ObjectId().toHexString(), currency = "EUR")
        }
    }

    @Test
    fun `throws appropriately when invalid currency is supplied`() {
        assertThrows<InvalidCurrencyFormatException> {
            updateOrderCurrency(orderId = "whatever", currency = "Not quite a currency darling")
        }
    }
}
