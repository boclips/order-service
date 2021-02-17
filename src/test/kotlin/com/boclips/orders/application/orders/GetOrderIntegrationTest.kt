package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidOrderRequest
import com.boclips.orders.config.security.UserRoles
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.security.testing.setSecurityContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import testsupport.TestFactories
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class GetOrderIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getOrder: GetOrder

    @Test
    fun `can get any order resource`() {
        val legacyOrder = TestFactories.legacyOrder()

        setSecurityContext("hq-user-id", UserRoles.VIEW_ORDERS)

        val order = OrderFactory.order(
            id = OrderId(legacyOrder.id),
            status = OrderStatus.INCOMPLETED,
            createdAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            updatedAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            items = listOf(OrderFactory.orderItem()),
            requestingUser = OrderFactory.completeOrderUser(
                userId = "different-user-id"
            )
        )
        ordersRepository.save(
            order = order
        )

        assertThat(getOrder(legacyOrder.id, userId = "hq-user-id")).isEqualTo(OrderResource.fromOrder(order))
    }

    @Test
    fun `can get own order resource`() {
        val legacyOrder = TestFactories.legacyOrder()

        setSecurityContext("web-app-user-id", UserRoles.VIEW_OWN_ORDERS)

        val order = OrderFactory.order(
            id = OrderId(legacyOrder.id),
            requestingUser = OrderFactory.completeOrderUser(userId = "web-app-user-id"),
            status = OrderStatus.INCOMPLETED,
            createdAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            updatedAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            items = listOf(OrderFactory.orderItem())
        )
        ordersRepository.save(
            order = order
        )

        assertThat(getOrder(legacyOrder.id, userId = "web-app-user-id" )).isEqualTo(OrderResource.fromOrder(order))
    }

    @Test
    fun `cannot get another users order resource`() {
        val legacyOrder = TestFactories.legacyOrder()

        setSecurityContext("web-app-user-id", UserRoles.VIEW_OWN_ORDERS)

        val order = OrderFactory.order(
            id = OrderId(legacyOrder.id),
            requestingUser = OrderFactory.completeOrderUser(userId = "different-user-id"),
            status = OrderStatus.INCOMPLETED,
            createdAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            updatedAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            items = listOf(OrderFactory.orderItem())
        )
        ordersRepository.save(
            order = order
        )

        assertThrows<OrderNotFoundException> {
            getOrder(legacyOrder.id, userId = "web-app-user-id" )
        }
    }

    @Test
    fun `throws an exception when there's no order`() {
        assertThrows<OrderNotFoundException> {
            getOrder("ohaim8", "user-id")
        }
    }

    @Test
    fun `throws an exception when ID not provided`() {
        assertThrows<InvalidOrderRequest> {
            getOrder(null, "user-id")
        }
    }
}
