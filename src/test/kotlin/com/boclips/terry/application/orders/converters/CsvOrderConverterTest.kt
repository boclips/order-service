package com.boclips.terry.application.orders.converters

import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories
import java.util.Date

class CsvOrderConverterTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderConverter: CsvOrderConverter

    @Test
    fun `groups orders by id`() {
        val csvOrderItems = listOf(
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
            TestFactories.csvOrderItemMetadata(legacyOrderId = "2")
        )

        val orders = orderConverter.toOrders(csvOrderItems)
        assertThat(orders).hasSize(2)
        assertThat(orders.map { it.legacyOrderId }).containsExactlyInAnyOrder("1", "2")
    }

    @Test
    fun `creates a list of order items for grouped order`() {
        val csvOrderItems = listOf(
            TestFactories.csvOrderItemMetadata(
                legacyOrderId = "1",
                title = "hello"
            ),
            TestFactories.csvOrderItemMetadata(
                legacyOrderId = "1",
                title = "good bye"
            )
        )

        val orders = orderConverter.toOrders(csvOrderItems)
        assertThat(orders).hasSize(1)
        assertThat(orders.first().items).hasSize(2)
        assertThat(orders.first().items.map { it.video.title }).containsExactlyInAnyOrder("hello", "good bye")
    }

    @Test
    fun `ignores order if they are invalid`() {
        val csvOrderItem = TestFactories.csvOrderItemMetadata(requestDate = null)

        val orders = orderConverter.toOrders(listOf(csvOrderItem))

        assertThat(orders).isEmpty()
    }

    @Test
    fun `order defaults to completed`() {
        val csvOrder = TestFactories.csvOrderItemMetadata()
        val orders = orderConverter.toOrders(listOf(csvOrder))

        assertThat(orders.first().status).isEqualTo(OrderStatus.COMPLETED)
    }

    @Test
    fun `sets order request date if present`() {
        val now = Date()
        val csvOrderItem = TestFactories.csvOrderItemMetadata(
            requestDate = now
        )

        val orders = orderConverter.toOrders(listOf(csvOrderItem))

        assertThat(orders.first().createdAt).isEqualTo(now.toInstant())
    }

    @Test
    fun `sets order updated date if present`() {
        val now = Date()
        val csvOrderItem = TestFactories.csvOrderItemMetadata(
            fulfilmentDate = now
        )

        val orders = orderConverter.toOrders(listOf(csvOrderItem))

        assertThat(orders.first().updatedAt).isEqualTo(now.toInstant())
    }

    @Test
    fun `sets isbn or product description`() {
        val csvOrderItem = TestFactories.csvOrderItemMetadata(isbnProductNumber = "hello")

        val orders = orderConverter.toOrders(listOf(csvOrderItem))

        assertThat(orders.first().isbnOrProductNumber).isEqualTo("hello")
    }

    @Test
    fun `sets requesting member`() {
        val csvOrderItem = TestFactories.csvOrderItemMetadata(memberRequest = "a great member")

        val orders = orderConverter.toOrders(listOf(csvOrderItem))

        assertThat(orders.first().requestingUser).isEqualTo(OrderUser.BasicUser("a great member"))
    }

    @Test
    fun `sets authorising member`() {
        val csvOrderItem = TestFactories.csvOrderItemMetadata(memberAuthorise = "a great member")

        val orders = orderConverter.toOrders(listOf(csvOrderItem))

        assertThat(orders.first().authorisingUser).isEqualTo(OrderUser.BasicUser("a great member"))
    }
}
