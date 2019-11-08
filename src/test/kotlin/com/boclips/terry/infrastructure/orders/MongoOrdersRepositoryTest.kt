package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.exceptions.OrderItemNotFoundException
import com.boclips.terry.domain.exceptions.OrderNotFoundException
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

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
    fun `orders are ordered by updated at`() {
        val firstUpdated = OrderFactory.order(updatedAt = Instant.ofEpochSecond(1))
        val lastUpdated = OrderFactory.order(updatedAt = Instant.ofEpochSecond(2))

        ordersRepository.save(order = firstUpdated)
        ordersRepository.save(order = lastUpdated)

        assertThat(ordersRepository.findAll().first()).isEqualTo(lastUpdated)
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
    fun `can update an order status`() {
        val order = OrderFactory.order(legacyOrderId = "legacy-id")
        ordersRepository.save(order = order)

        ordersRepository.update(OrderUpdateCommand.ReplaceStatus(orderId = order.id, orderStatus = OrderStatus.INVALID))

        assertThat(ordersRepository.findOne(order.id)!!.status).isEqualTo(OrderStatus.INVALID)
    }

    @Test
    fun `updates to order update the updated at time`() {
        val startOfTest = Instant.now().minusMillis(100)
        val order = OrderFactory.order(legacyOrderId = "legacy-id", updatedAt = startOfTest)
        ordersRepository.save(order = order)
        ordersRepository.update(OrderUpdateCommand.ReplaceStatus(orderId = order.id, orderStatus = OrderStatus.INVALID))

        assertThat(ordersRepository.findOne(order.id)!!.updatedAt).isAfter(startOfTest)
    }

    @Test
    fun `throws when updating a non existent order`() {
        assertThrows<OrderNotFoundException> {
            ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = OrderId(ObjectId().toHexString()),
                    orderStatus = OrderStatus.INVALID
                )
            )

        }
    }

    @Test
    fun `throws when updating an order with a invalid ID`() {
        assertThrows<OrderNotFoundException> {
            ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = OrderId("you cheeky programmer"),
                    orderStatus = OrderStatus.INVALID
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
            OrderUpdateCommand.UpdateOrderItemsCurrency(
                orderId = originalOrder.id,
                currency = Currency.getInstance("EUR")
            )
        )

        assertThat(updatedOrder.currency).isEqualTo(Currency.getInstance("EUR"))
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
