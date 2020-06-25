package com.boclips.orders.domain.model

import com.boclips.orders.domain.exceptions.IllegalCurrencyException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import java.util.Currency

class OrderTest {

    @Test
    fun `Throws when item is added with different currency`() {
        OrderFactory.order(currency = Currency.getInstance("PLN")).apply {
            assertThrows<IllegalCurrencyException> {
                addItem(
                    OrderFactory.orderItem(
                        price = PriceFactory.onePound()
                    )
                )
            }
        }
    }

    @Test
    fun `Throws when item is added and currency is null`() {
        OrderFactory.order(currency = null).apply {
            assertThrows<IllegalCurrencyException> {
                addItem(
                    OrderFactory.orderItem(
                        price = PriceFactory.onePound()
                    )
                )
            }
        }
    }

    @Test
    fun `can calculate total cost of an order`() {
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(price = PriceFactory.tenDollars()),
                OrderFactory.orderItem(price = PriceFactory.tenDollars()),
                OrderFactory.orderItem(price = PriceFactory.tenDollars())
            )
        )

        assertThat(order.totalPrice).isEqualTo(BigDecimalWith2DP.valueOf(30))
    }

    @Test
    fun `defaults missing price to 0`() {
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(price = PriceFactory.tenDollars()),
                OrderFactory.orderItem(
                    price = Price(amount = null, currency = Currency.getInstance("USD"))
                )
            )
        )

        assertThat(order.totalPrice).isEqualTo(BigDecimalWith2DP.valueOf(10))
    }
}
