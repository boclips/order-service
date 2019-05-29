package com.boclips.terry.domain

import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.time.Instant

class OrderServiceTest {
    @Test
    fun `it can retrieve all the orders`() {
        val repo = FakeOrdersRepository()

        val id1 = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id1)
        val order1 = TestFactories.order(
            legacyOrder,
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH
        )
        repo.add(order1, TestFactories.legacyOrderDocument(legacyOrder, "creator@theworld.example", "some@vendor.4u"))

        val id2 = ObjectId().toHexString()
        val legacyOrder2 = TestFactories.legacyOrder(id2)
        val order2 = TestFactories.order(
            legacyOrder,
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH
        )
        repo.add(order2, TestFactories.legacyOrderDocument(legacyOrder2, "creator@theworld.example", "some@vendor.4u"))

        val service = OrderService(repo)
        assertThat(service.findAll()).isEqualTo(listOf(order1, order2))
    }
}
