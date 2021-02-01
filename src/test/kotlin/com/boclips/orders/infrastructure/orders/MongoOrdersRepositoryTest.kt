package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.exceptions.OrderItemNotFoundException
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Duration
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.AbstractSpringIntegrationTest
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.OrderFactory.completeOrderUser
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Currency

class MongoOrdersRepositoryTest : AbstractSpringIntegrationTest() {

    @Test
    fun `creates an order`() {
        val order = OrderFactory.order()

        ordersRepository.save(order = order)

        val orderFromMongo = ordersRepository.findAll()

        assertThat(orderFromMongo[0].id.value).isEqualTo(order.id.value)
        assertThat(orderFromMongo[0].legacyOrderId).isEqualTo(order.legacyOrderId)
        assertThat(orderFromMongo[0].authorisingUser).isEqualTo(order.authorisingUser)
    }

    @Test
    fun `gets descending paginated user orders`() {

        ordersRepository.save(
            order = OrderFactory.order(
                createdAt = Instant.now().plus(5, ChronoUnit.SECONDS),
                isbnOrProductNumber = "order-1",
                requestingUser = completeOrderUser(userId = "1234")
            )
        )
        ordersRepository.save(
            order = OrderFactory.order(
                createdAt = Instant.now().plus(10, ChronoUnit.SECONDS),
                isbnOrProductNumber = "order-2",
                requestingUser = completeOrderUser(userId = "1234")
            )
        )
        ordersRepository.save(
            order = OrderFactory.order(
                createdAt = Instant.now().plus(15, ChronoUnit.SECONDS),
                isbnOrProductNumber = "order-3",
                requestingUser = completeOrderUser(userId = "1234")
            )
        )
        ordersRepository.save(
            order = OrderFactory.order(
                createdAt = Instant.now().plus(20, ChronoUnit.SECONDS),
                isbnOrProductNumber = "order-4",
                requestingUser = completeOrderUser(userId = "1234")
            )
        )
        ordersRepository.save(
            order = OrderFactory.order(
                createdAt = Instant.now().plus(25, ChronoUnit.SECONDS),
                isbnOrProductNumber = "order-5",
                requestingUser = completeOrderUser(userId = "1234")
            )
        )
        ordersRepository.save(
            order = OrderFactory.order(
                createdAt = Instant.now().plus(30, ChronoUnit.SECONDS),
                isbnOrProductNumber = "order-6",
                requestingUser = completeOrderUser(userId = "1234")
            )
        )

        ordersRepository.save(
            order = OrderFactory.order(
                isbnOrProductNumber = "different",
                requestingUser = completeOrderUser(userId = "differentUser")
            )
        )

        val page = ordersRepository.getPaginated(pageSize = 3, pageNumber = 0, userId = "1234").orders
        assertThat(page).hasSize(3)
        assertThat(page[0].isbnOrProductNumber).isEqualTo("order-6")
        assertThat(page[2].isbnOrProductNumber).isEqualTo("order-4")

        val page2 = ordersRepository.getPaginated(pageSize = 3, pageNumber = 1, userId = "1234").orders
        assertThat(page2).hasSize(3)
        assertThat(page2[0].isbnOrProductNumber).isEqualTo("order-3")
        assertThat(page2[2].isbnOrProductNumber).isEqualTo("order-1")

        assertThat(
            ordersRepository.getPaginated(
                pageSize = 5,
                pageNumber = 0,
                userId = "1234"
            ).totalElements
        ).isEqualTo(6)
    }

    @Test
    fun `can get order by id`() {
        val id = ObjectId().toHexString()
        val order = OrderFactory.order(
            id = OrderId(value = id)
        )

        ordersRepository.save(order = order)

        val foundOrder = ordersRepository.findOne(OrderId(value = id))

        assertThat(foundOrder?.id?.value).isEqualTo(order.id.value)
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

        assertThat(ordersRepository.findAll().first().id.value).isEqualTo(lastCreated.id.value)
    }

    @Test
    fun `can find order by legacy id`() {
        val order = OrderFactory.order(legacyOrderId = "legacy-id")
        val ignoredOrder = OrderFactory.order(legacyOrderId = "other-legacy-id")
        ordersRepository.save(order = order)
        ordersRepository.save(order = ignoredOrder)

        val retrievedOrder = ordersRepository.findOneByLegacyId("legacy-id")

        assertThat(retrievedOrder?.legacyOrderId).isEqualTo(order.legacyOrderId)
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
    fun `updates delivery date of order`() {
        val startOfTest = Instant.now().minusMillis(100)
        val order = OrderFactory.order(deliveredAt = null)
        ordersRepository.save(order = order)
        ordersRepository.update(OrderUpdateCommand.ReplaceDeliveredAt(orderId = order.id, deliveredAt = Instant.now()))

        assertThat(ordersRepository.findOne(order.id)!!.deliveredAt).isAfter(startOfTest)
    }

    @Test
    fun `updates delivery date with null`() {
        val order = OrderFactory.order(deliveredAt = Instant.now())
        ordersRepository.save(order = order)
        ordersRepository.update(OrderUpdateCommand.ReplaceDeliveredAt(orderId = order.id, deliveredAt = null))

        assertThat(ordersRepository.findOne(order.id)!!.deliveredAt).isNull()
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
                    OrderFactory.orderItem(id = "2", license = OrderFactory.orderItemLicense(territory = "A Castle"))
                )
            )
        )

        val updatedOrder = ordersRepository.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemTerritory(
                orderId = originalOrder.id,
                orderItemsId = "2",
                territory = "A park"
            )
        )

        assertThat(updatedOrder.items.first { it.id == "2" }.license!!.territory).isEqualTo("A park")
    }

    @Test
    fun `can update the duration order item`() {
        val originalOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(id = "1"),
                    OrderFactory.orderItem(
                        id = "2",
                        license = OrderFactory.orderItemLicense(duration = Duration.Description("1 Year"))
                    )
                )
            )
        )

        val updatedOrder = ordersRepository.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemDuration(
                orderId = originalOrder.id,
                orderItemsId = "2",
                duration = "5 Years"
            )
        )

        assertThat(updatedOrder.items.first { it.id == "2" }.license!!.duration).isEqualTo(Duration.Description("5 Years"))
    }

    @Test
    fun `can update the caption status of an order item`() {
        val originalOrder = ordersRepository.save(
            OrderFactory.order(
                items = listOf(
                    OrderFactory.orderItem(
                        id = "1",
                        video = TestFactories.video(captionStatus = AssetStatus.UNAVAILABLE)
                    ),
                    OrderFactory.orderItem(
                        id = "2",
                        video = TestFactories.video(captionStatus = AssetStatus.UNAVAILABLE)
                    )
                )
            )
        )

        val updatedOrder = ordersRepository.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateCaptionStatus(
                orderId = originalOrder.id,
                orderItemId = "1",
                captionStatus = AssetStatus.REQUESTED
            )
        )

        assertThat(updatedOrder.items.first { it.id == "1" }.video.captionStatus).isEqualTo(AssetStatus.REQUESTED)
    }

    @Test
    fun `can bulk update many orders`() {
        val originalOrder1 = ordersRepository.save(
            OrderFactory.order(
                currency = Currency.getInstance("GBP"),
                orderOrganisation = OrderOrganisation("bad org")
            )
        )
        val originalOrder2 = ordersRepository.save(OrderFactory.order(currency = Currency.getInstance("USD")))

        ordersRepository.bulkUpdate(
            listOf(
                OrderUpdateCommand.UpdateOrderCurrency(
                    orderId = originalOrder1.id,
                    currency = Currency.getInstance("EUR"),
                    fxRateToGbp = BigDecimal.ONE
                ),
                OrderUpdateCommand.UpdateOrderOrganisation(
                    orderId = originalOrder1.id,
                    organisation = OrderOrganisation("wow org")
                ),
                OrderUpdateCommand.UpdateOrderCurrency(
                    orderId = originalOrder2.id,
                    currency = Currency.getInstance("EUR"),
                    fxRateToGbp = BigDecimal.ZERO
                )
            )
        )

        val update1 = ordersRepository.findOne(originalOrder1.id)!!
        val update2 = ordersRepository.findOne(originalOrder2.id)!!

        assertThat(update1.currency).isEqualTo(Currency.getInstance("EUR"))
        assertThat(update1.organisation?.name).isEqualTo("wow org")
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
