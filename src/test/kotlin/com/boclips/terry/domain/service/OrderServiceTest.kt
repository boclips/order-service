package com.boclips.terry.domain.service

import com.boclips.terry.application.orders.IllegalOrderStateExport
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.Price
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.util.Currency

class OrderServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderService: OrderService

    @Test
    fun `can create an order`() {
        val originalOrder = OrderFactory.order()

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)

        assertThat(originalOrder).isEqualTo(retrievedOrder)
    }

    @Test
    fun `a created order is complete if it has a currency and all items have a price`() {
        val originalOrder = OrderFactory.order(
            status = OrderStatus.INCOMPLETED,
            items = listOf(OrderFactory.orderItem(price = PriceFactory.tenDollars()))
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.COMPLETED)
    }

    @Test
    fun `cannot replace status of an order to complete if it's not completed`() {
        val order = OrderFactory.order(
            status = OrderStatus.COMPLETED,
            items = listOf(OrderFactory.orderItem(price = Price(amount = null, currency = null)))
        )

        orderService.createIfNonExistent(order)

        val retreivedOrder = ordersRepository.findOne(order.id)!!

        assertThat(retreivedOrder.status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `a cancelled order can not been complete`() {
        val originalOrder = OrderFactory.order(
            status = OrderStatus.CANCELLED,
            items = listOf(OrderFactory.orderItem(price = PriceFactory.tenDollars()))
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.CANCELLED)
    }

    @Test
    fun `ignores orders with a clashing legacy id`() {
        ordersRepository.save(OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.INCOMPLETED))

        val newOrder = OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.CANCELLED)

        orderService.createIfNonExistent(newOrder)

        val retrievedOrders = ordersRepository.findAll()

        assertThat(retrievedOrders).hasSize(1)
        assertThat(retrievedOrders.first().status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `when any order has status incomplete, throws`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.INCOMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        assertThrows<IllegalOrderStateExport> {
            orderService.exportManifest(emptyMap())
        }
    }

    @Test
    fun `when any orders are cancelled they are filtered`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.CANCELLED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.COMPLETED,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        val manifest = orderService.exportManifest(
            mapOf(
                Currency.getInstance("USD") to BigDecimal.TEN,
                Currency.getInstance("GBP") to BigDecimal.ONE
            )
        )
        assertThat(manifest.items).hasSize(2)
    }

    @Test
    fun `exports manifest with correct fx rates`() {
        val order =
            OrderFactory.order(
                status = OrderStatus.COMPLETED, items = listOf(
                    OrderFactory.orderItem(
                        price = PriceFactory.tenDollars(),
                        video = TestFactories.video(
                            contentPartner = TestFactories.contentPartner(
                                currency = Currency.getInstance(
                                    "SGD"
                                )
                            )
                        )
                    )

                )
            )

        orderService.createIfNonExistent(order)

        val manifest = orderService.exportManifest(
            fxRatesAgainstPound = mapOf(
                Currency.getInstance("USD") to BigDecimal.valueOf(4),
                Currency.getInstance("SGD") to BigDecimal.valueOf(2)
            )
        )

        assertThat(manifest.items).hasSize(1)
        assertThat(manifest.items.first().fxRate).isEqualTo(BigDecimal.valueOf(0.50000).setScale(5))
        assertThat(manifest.items.first().convertedSalesAmount.amount).isEqualTo(BigDecimalWith2DP.valueOf(5))
        assertThat(manifest.items.first().convertedSalesAmount.currency).isEqualTo(Currency.getInstance("SGD"))
    }

    @Test
    fun `updating an order also updates its status`() {
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(
                    price = Price(
                        amount = null,
                        currency = Currency.getInstance("USD")
                    )
                )
            ),
            status = OrderStatus.INCOMPLETED
        )

        orderService.createIfNonExistent(order)

        val updatedOrder = orderService.update(
            OrderUpdateCommand.UpdateOrderItemPrice(
                order.id,
                order.items.first().id,
                BigDecimal.valueOf(100)
            )
        )

        assertThat(updatedOrder.status).isEqualTo(OrderStatus.COMPLETED)
    }

    @Test
    fun `Orders are converted to their CP's currency`() {
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(
                    price = Price(
                        amount = BigDecimal.valueOf(100),
                        currency = Currency.getInstance("USD")
                    )
                )
            ),
            status = OrderStatus.INCOMPLETED
        )

        orderService.createIfNonExistent(order)

        val updatedOrder = orderService.update(
            OrderUpdateCommand.UpdateOrderItemPrice(
                order.id,
                order.items.first().id,
                BigDecimal.valueOf(100)
            )
        )

        assertThat(updatedOrder.status).isEqualTo(OrderStatus.COMPLETED)
    }
}
