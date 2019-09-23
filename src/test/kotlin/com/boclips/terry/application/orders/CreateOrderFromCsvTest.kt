package com.boclips.terry.application.orders

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.OrderFactory
import testsupport.TestFactories
import java.time.LocalDateTime
import java.time.ZoneOffset

class CreateOrderFromCsvTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var createOrderFromCsv: CreateOrderFromCsv

    @Test
    fun `can create an order`() {
        val csvOrderMetadata = TestFactories.csvOrderItemMetadata()

        this.defaultVideoClientResponse(csvOrderMetadata.videoId)

        createOrderFromCsv.invoke(listOf(csvOrderMetadata))

        val createdOrder = ordersRepository.findOneByLegacyId(csvOrderMetadata.legacyOrderId)

        assertThat(createdOrder).isNotNull
    }

    @Test
    fun `ignores orders with the clashing legacy order ids`() {
        this.defaultVideoClientResponse()
        val orderToBeCreated = OrderFactory.order(
            legacyOrderId = "1",
            updatedAt = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.UTC)
        )
        ordersRepository.save(orderToBeCreated)

        val csvOrderMetadata =
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1")

        createOrderFromCsv.invoke(listOf(csvOrderMetadata))

        val createdOrders = ordersRepository.findAll()
        assertThat(createdOrders).hasSize(1)
        assertThat(createdOrders.first()).isEqualTo(orderToBeCreated)
    }
}
