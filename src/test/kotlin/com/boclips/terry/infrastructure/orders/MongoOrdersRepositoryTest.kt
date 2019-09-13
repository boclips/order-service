package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.infrastructure.orders.exceptions.OrderNotFoundException
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.TestFactories
import java.time.Instant

class MongoOrdersRepositoryTest : AbstractSpringIntegrationTest() {

    @Test
    fun `creates an order`() {
        val order = TestFactories.order()

        ordersRepository.add(order = order)
        assertThat(ordersRepository.findAll()).containsExactly(order)
    }

    @Test
    fun `can get order by id`() {
        val id = ObjectId().toHexString()
        val order = TestFactories.order(
            id = OrderId(value = id)
        )

        ordersRepository.add(order = order)

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
        val firstUpdated = TestFactories.order(updatedAt = Instant.ofEpochSecond(1))
        val lastUpdated = TestFactories.order(updatedAt = Instant.ofEpochSecond(2))

        ordersRepository.add(order = firstUpdated)
        ordersRepository.add(order = lastUpdated)

        assertThat(ordersRepository.findAll().first()).isEqualTo(lastUpdated)
    }

    @Test
    fun `can find order by legacy id`() {
        val order = TestFactories.order(legacyOrderId = "legacy-id")
        val ignoredOrder = TestFactories.order(legacyOrderId = "other-legacy-id")
        ordersRepository.add(order = order)
        ordersRepository.add(order = ignoredOrder)

        val retrievedOrder = ordersRepository.findOneByLegacyId("legacy-id")

        assertThat(retrievedOrder).isEqualTo(order)
    }

    @Test
    fun `can update an order status`() {
        val order = TestFactories.order(legacyOrderId = "legacy-id")
        ordersRepository.add(order = order)

        ordersRepository.update(OrderUpdateCommand.ReplaceStatus(orderId = order.id, orderStatus = OrderStatus.INVALID))

        assertThat(ordersRepository.findOne(order.id)!!.status).isEqualTo(OrderStatus.INVALID)
    }

    @Test
    fun `updates to order update the updated at time`() {
        val startOfTest = Instant.now().minusMillis(100)
        val order = TestFactories.order(legacyOrderId = "legacy-id", updatedAt = startOfTest)
        ordersRepository.add(order = order)
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
}
