package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.exceptions.OrderItemNotFoundException
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class MongoOrdersRepositoryTest : AbstractSpringIntegrationTest() {

    @Test
    fun `creates an order`() {
        val order = OrderFactory.order()

        ordersRepository.save(order = order)
        assertThat(ordersRepository.findAll()).containsExactly(order)
    }

    @Test
    fun `can get order by id`() {
        val id = ObjectId().toHexString()
        val order = OrderFactory.order(
            id = OrderId(value = id)
        )

        ordersRepository.save(order = order)

        assertThat(ordersRepository.findOne(OrderId(value = id))).isEqualTo(order)
    }

    @Test
    fun `returns null when id is invalid`() {
        val id = OrderId(value = "invalid")

        val order = ordersRepository.findOne(id)

        assertThat(order).isNull()
    }

    @Test
    fun `orders are ordered by created at`() {
        val firstCreated = OrderFactory.order(createdAt = Instant.ofEpochSecond(1))
        val lastCreated = OrderFactory.order(createdAt = Instant.ofEpochSecond(2))

        ordersRepository.save(order = firstCreated)
        ordersRepository.save(order = lastCreated)

        assertThat(ordersRepository.findAll().first()).isEqualTo(lastCreated)
    }

    @Test
    fun `can find order by legacy id`() {
        val order = OrderFactory.order(legacyOrderId = "legacy-id")
        val ignoredOrder = OrderFactory.order(legacyOrderId = "other-legacy-id")
        ordersRepository.save(order = order)
        ordersRepository.save(order = ignoredOrder)

        val retrievedOrder = ordersRepository.findOneByLegacyId("legacy-id")

        assertThat(retrievedOrder).isEqualTo(order)
    }

    @Test
    fun `can cancel an order`() {
        val order = OrderFactory.order(legacyOrderId = "legacy-id")
        ordersRepository.save(order = order)

        ordersRepository.update(OrderUpdateCommand.SetOrderCancellation(orderId = order.id, cancelled = true))

        assertThat(ordersRepository.findOne(order.id)!!.cancelled).isEqualTo(true)
    }

    @Test
    fun `updates to order update the updated at time`() {
        val startOfTest = Instant.now().minusMillis(100)
        val order = OrderFactory.order(legacyOrderId = "legacy-id", updatedAt = startOfTest)
        ordersRepository.save(order = order)
        ordersRepository.update(OrderUpdateCommand.SetOrderCancellation(orderId = order.id, cancelled = true))

        assertThat(ordersRepository.findOne(order.id)!!.updatedAt).isAfter(startOfTest)
    }

    @Test
    fun `throws when updating a non existent order`() {
        assertThrows<OrderNotFoundException> {
            ordersRepository.update(
                OrderUpdateCommand.SetOrderCancellation(
                    orderId = OrderId(ObjectId().toHexString()),
                    cancelled = true
                )
            )

        }
    }

    @Test
    fun `throws when updating an order with a invalid ID`() {
        assertThrows<OrderNotFoundException> {
            ordersRepository.update(
                OrderUpdateCommand.SetOrderCancellation(
                    orderId = OrderId("you cheeky programmer"),
                    cancelled = true
                )
            )

        }
    }

    @Test
    fun `can update the currency for order items`() {
        val originalOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(price = PriceFactory.onePound()),
                    OrderFactory.orderItem(price = PriceFactory.onePound())
                )
            )
        )

        val updatedOrder = ordersRepository.update(
            OrderUpdateCommand.UpdateOrderCurrency(
                orderId = originalOrder.id,
                currency = Currency.getInstance("EUR"),
                fxRateToGbp = BigDecimal("1.25")
            )
        )

        assertThat(updatedOrder.currency).isEqualTo(Currency.getInstance("EUR"))
        assertThat(updatedOrder.fxRateToGbp).isEqualTo("1.25")
        assertThat(updatedOrder.items.map { it.price.currency.toString() }).allMatch { it == "EUR" }
    }

    @Test
    fun `can update the price for an order item`() {
        val originalOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(id = "1", price = PriceFactory.onePound()),
                    OrderFactory.orderItem(id = "2", price = PriceFactory.onePound())
                )
            )
        )

        val updatedOrder = ordersRepository.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice(
                orderId = originalOrder.id,
                orderItemsId = "2",
                amount = BigDecimal.valueOf(10)
            )
        )

        assertThat(updatedOrder.items.first { it.id == "2" }.price.amount).isEqualTo(BigDecimalWith2DP.valueOf(10))
    }

    @Test
    fun `can update the territory order item`() {
        val originalOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(id = "1"),
                    OrderFactory.orderItem(id = "2", license = OrderFactory.orderItemLicense(territory = "1 Year"))
                )
            )
        )

        val updatedOrder = ordersRepository.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemLicense(
                orderId = originalOrder.id,
                orderItemsId = "2",
                orderItemLicense = OrderItemLicense(duration = Duration.Description("5 Years"), territory = "A park")
            )
        )

        assertThat(updatedOrder.items.first { it.id == "2" }.license!!.duration).isEqualTo(Duration.Description("5 Years"))
        assertThat(updatedOrder.items.first { it.id == "2" }.license!!.territory).isEqualTo("A park")
    }

    @Test
    fun `can bulk update many orders`() {
        val originalOrder1 = ordersRepository.save(OrderFactory.order(currency = Currency.getInstance("GBP")))
        val originalOrder2 = ordersRepository.save(OrderFactory.order(currency = Currency.getInstance("USD")))

        ordersRepository.bulkUpdate(
            listOf(
                OrderUpdateCommand.UpdateOrderCurrency(orderId = originalOrder1.id, currency = Currency.getInstance("EUR"), fxRateToGbp = BigDecimal.ONE),
                OrderUpdateCommand.UpdateOrderCurrency(orderId = originalOrder2.id, currency = Currency.getInstance("EUR"), fxRateToGbp = BigDecimal.ZERO)
            )
        )

        val update1 = ordersRepository.findOne(originalOrder1.id)!!
        val update2 = ordersRepository.findOne(originalOrder2.id)!!

        assertThat(update1.currency).isEqualTo(Currency.getInstance("EUR"))
        assertThat(update2.currency).isEqualTo(Currency.getInstance("EUR"))
    }

    @Test
    fun `can update a video`() {
        val orderItem = OrderFactory.orderItem(video = TestFactories.video(title = "HI"))
        val order = ordersRepository.save(OrderFactory.order(items = listOf(orderItem)))

        val newOrder = ordersRepository.update(
            OrderUpdateCommand.OrderItemUpdateCommand.ReplaceVideo(
                orderId = order.id,
                orderItemsId = orderItem.id,
                video = TestFactories.video(title = "Blue Monday")
            )
        )

        assertThat(newOrder.items.first().video.title).isEqualTo("Blue Monday")
    }

    @Test
    fun `it throws when updating an non-existent order-item`() {
        val originalOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(id = "2", price = PriceFactory.onePound())
                )
            )
        )

        assertThrows<OrderItemNotFoundException> {
            ordersRepository.update(
                OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice(
                    orderId = originalOrder.id,
                    orderItemsId = "non-existent",
                    amount = BigDecimal.valueOf(10)
                )
            )
        }
    }
}
