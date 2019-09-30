package com.boclips.terry.domain.model

import com.boclips.terry.domain.exceptions.IllegalCurrencyException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import java.math.BigDecimal
import java.util.Currency

class OrderTest {

    @Test
    fun `getCurrency returns currency of underlying items`() {
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(
                    price = PriceFactory.tenDollars()
                )
            )
        )

        assertThat(order.currency).isEqualTo(Currency.getInstance("USD"))
    }

    @Test
    fun `getCurrency returns null if no underlying items`() {
        val order = OrderFactory.order(
            items = emptyList()
        )

        assertThat(order.currency).isNull()
    }

    @Test
    fun `Throws when item is added with different currency`() {
        OrderFactory.order().apply {
            addItem(
                OrderFactory.orderItem(
                    price = PriceFactory.tenDollars()
                )
            )

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
