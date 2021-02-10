package com.boclips.orders.application.orders

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory

class GetOrdersIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getOrders: GetOrders

    @Test
    fun `can get a list of order resources`() {
        ordersRepository.save(OrderFactory.order(legacyOrderId = "hello"))
        ordersRepository.save(OrderFactory.order(legacyOrderId = "bye"))

        val retrievedOrders = getOrders.getAll()

        assertThat(retrievedOrders.map { it.searchableOrderId })
            .containsExactlyInAnyOrder("hello", "bye")
    }

    @Test
    fun `can get paginated a list of order resources`() {
        ordersRepository.save(
            OrderFactory.order(
                legacyOrderId = "hello",
                requestingUser = OrderFactory.completeOrderUser(
                    userId = "1234"
                )
            )
        )
        ordersRepository.save(
            OrderFactory.order(
                legacyOrderId = "bye",
                requestingUser = OrderFactory.completeOrderUser(
                    userId = "1234"
                )
            )
        )

        val retrievedOrders = getOrders.getPaginated(10, 0, "1234")

        assertThat(retrievedOrders.elements.map { it.legacyOrderId })
            .containsExactlyInAnyOrder("hello", "bye")
    }
}
