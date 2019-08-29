package com.boclips.terry.domain

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.service.OrderService
import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant

class OrderServiceTest {
    @Test
    fun `it can retrieve all the orders`() {
        val repo = FakeOrdersRepository()

        val id1 = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id = id1)
        val order1 = TestFactories.order(
            id = OrderId(value = legacyOrder.id),
            orderProviderId = "an-provider-id",
            status = OrderStatus.CONFIRMED,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH,
            items = listOf(TestFactories.orderItem())
        )

        repo.add(
            order1
        )

        val id2 = ObjectId().toHexString()
        val legacyOrder2 = TestFactories.legacyOrder(id2)
        val order2 = TestFactories.order(
            id = OrderId(value = legacyOrder2.id),
            orderProviderId = "an-provider-id",
            status = OrderStatus.CONFIRMED,
            updatedAt = Instant.EPOCH,
            createdAt = Instant.EPOCH,
            items = listOf(TestFactories.orderItem())
        )
        repo.add(
            order2
        )

        val service = OrderService(repo)
        assertThat(service.findAll()).isEqualTo(listOf(order1, order2))
    }
}
