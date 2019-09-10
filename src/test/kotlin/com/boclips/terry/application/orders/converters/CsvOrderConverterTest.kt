package com.boclips.terry.application.orders.converters

import com.boclips.terry.domain.model.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class CsvOrderConverterTest {
    @Test
    fun `groups orders by id`() {
        val csvOrderItems = listOf(
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1"),
            TestFactories.csvOrderItemMetadata(legacyOrderId = "2")
        )

        val orders = CsvOrderConverter.toOrders(csvOrderItems)
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

        val orders = CsvOrderConverter.toOrders(csvOrderItems)
        assertThat(orders).hasSize(1)
        assertThat(orders.first().items).hasSize(2)
        assertThat(orders.first().items.map { it.video.title }).containsExactlyInAnyOrder("hello", "good bye")
    }

    @Test
    fun `order defaults to completed`() {
        val csvOrder = TestFactories.csvOrderItemMetadata()
        val orders = CsvOrderConverter.toOrders(listOf(csvOrder))

        assertThat(orders.first().status).isEqualTo(OrderStatus.COMPLETED)
    }
}