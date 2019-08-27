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
            OrderId(value = legacyOrder.id),
            "an-provider-id",
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(
                OrderItem(
                    uuid = "hi-again-its-a-uuid",
                    price = BigDecimal.ONE,
                    transcriptRequested = true,
                    contentPartner = TestFactories.contentPartner(),
                    video = TestFactories.video()
                )
            )
        )

        repo.add(
            order1
        )

        val id2 = ObjectId().toHexString()
        val legacyOrder2 = TestFactories.legacyOrder(id2)
        val order2 = TestFactories.order(
            OrderId(value = legacyOrder2.id),
            "an-provider-id",
            "boclips",
            "big-bang",
            OrderStatus.CONFIRMED,
            Instant.EPOCH,
            Instant.EPOCH,
            items = listOf(
                OrderItem(
                    uuid = "oh-guess-what-its-a-uuid",
                    price = BigDecimal.ONE,
                    transcriptRequested = true,
                    contentPartner = TestFactories.contentPartner(),
                    video = TestFactories.video()
                )
            )
        )
        repo.add(
            order2
        )

        val service = OrderService(repo)
        assertThat(service.findAll()).isEqualTo(listOf(order1, order2))
    }
}
