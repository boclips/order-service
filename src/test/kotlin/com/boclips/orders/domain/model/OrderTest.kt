package com.boclips.orders.domain.model

import com.boclips.orders.domain.exceptions.IllegalCurrencyException
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import java.math.BigDecimal
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
            currency = Currency.getInstance("USD"),
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
            currency = Currency.getInstance("USD"),
            items = listOf(
                OrderFactory.orderItem(price = PriceFactory.tenDollars()),
                OrderFactory.orderItem(
                    price = Price(amount = null, currency = Currency.getInstance("USD"))
                )
            )
        )

        assertThat(order.totalPrice).isEqualTo(BigDecimalWith2DP.valueOf(10))
    }

    @Nested
    inner class `order status`{

        @Test
        internal fun `order is cancelled`() {
            val order = OrderFactory.order(cancelled = true)

            assertThat(order.status).isEqualTo(OrderStatus.CANCELLED)
        }

        @Test
        fun `an order with missing item license is not complete`() {
            val order = OrderFactory.order(
                currency = Currency.getInstance("GBP"),
                items = listOf(
                    OrderFactory.orderItem(
                        price = PriceFactory.tenPounds(),
                        license = null
                    )
                )
            )

            assertThat(order.status).isEqualTo(OrderStatus.INCOMPLETED)
        }

        @Test
        fun `an order with missing item price is not complete`() {
            val order = OrderFactory.order(
                currency = Currency.getInstance("GBP"),
                items = listOf(
                    OrderFactory.orderItem(
                        price = Price(
                            amount = null,
                            currency = Currency.getInstance("GBP")
                        ),
                        license = OrderItemLicense(duration = Duration.Description("5 years"), territory = "UK")
                    )
                )
            )

            assertThat(order.status).isEqualTo(OrderStatus.INCOMPLETED)
        }

        @Test
        fun `an order with missing item price currency is not complete`() {
            val order = OrderFactory.order(
                currency = null,
                items = listOf(
                    OrderFactory.orderItem(
                        price = Price(
                            amount = BigDecimal.TEN,
                            currency = null
                        ),
                        license = OrderItemLicense(duration = Duration.Description("5 years"), territory = "UK")
                    )
                )
            )

            assertThat(order.status).isEqualTo(OrderStatus.INCOMPLETED)
        }

        @Test
        fun `a created order is complete if it has a currency and all items have a price and license`() {
            val order = OrderFactory.order(
                currency = Currency.getInstance("GBP"),
                items = listOf(
                    OrderFactory.orderItem(
                        price = PriceFactory.tenPounds(),
                        license = OrderItemLicense(duration = Duration.Description("5 years"), territory = "UK")
                    )
                )
            )

            assertThat(order.status).isEqualTo(OrderStatus.COMPLETED)
        }
    }
}
