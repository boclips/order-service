package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.presentation.resources.OrderResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories
import java.time.Instant

class GetOrdersIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getOrders: GetOrders

    @Test
    fun `can get a list of order resources`() {
        val legacyOrder = TestFactories.legacyOrder()

        val now = Instant.EPOCH
        val order1 = TestFactories.order(
            id = OrderId(value = legacyOrder.id),
            status = OrderStatus.CONFIRMED,
            createdAt = now.plusMillis(6),
            updatedAt = now.plusMillis(5),
            items = listOf(TestFactories.orderItem())
        )
        fakeOrdersRepository.add(
            order = order1
        )

        val order2 = TestFactories.order(
            id = OrderId(value = legacyOrder.id),
            status = OrderStatus.CONFIRMED,
            createdAt = now.plusMillis(3),
            updatedAt = now.plusMillis(4),
            items = listOf(TestFactories.orderItem())
        )
        fakeOrdersRepository.add(
            order = order2
        )

        assertThat(getOrders())
            .isEqualTo(
                listOf(order1, order2)
                    .map(OrderResource.Companion::fromOrder)
            )
    }
}
