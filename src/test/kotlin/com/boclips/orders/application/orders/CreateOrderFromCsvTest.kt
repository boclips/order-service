package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidCsvException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import testsupport.TestFactories
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class CreateOrderFromCsvTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var createOrderFromCsv: CreateOrderFromCsv

    @Test
    fun `creates orders if they are new`() {
        val csvOrderMetadata = TestFactories.csvOrderItemMetadata()
        this.defaultVideoClientResponse(csvOrderMetadata.videoId!!)

        createOrderFromCsv.invoke(listOf(csvOrderMetadata))

        val createdOrder = ordersRepository.findOneByLegacyId(csvOrderMetadata.legacyOrderId!!)
        assertThat(createdOrder).isNotNull
    }

    @Test
    fun `throws and ignores order if invalid`() {
        val csvOrderMetadata = TestFactories.csvOrderItemMetadata(requestDate = "dubious date")
        this.defaultVideoClientResponse(csvOrderMetadata.videoId!!)

        assertThrows<InvalidCsvException> {
            createOrderFromCsv.invoke(listOf(csvOrderMetadata))
        }

        val createdOrder = ordersRepository.findOneByLegacyId(csvOrderMetadata.legacyOrderId!!)
        assertThat(createdOrder).isNull()
    }

    @Test
    fun `ignores orders with the clashing legacy order ids`() {
        defaultVideoClientResponse(videoId = "123")
        val orderToBeCreated = OrderFactory.order(
            legacyOrderId = "1",
            updatedAt = LocalDateTime.of(2011, 1, 1, 1, 1).toInstant(ZoneOffset.UTC),
            currency = Currency.getInstance("GBP")
        )

        ordersRepository.save(orderToBeCreated)

        val csvOrderMetadata =
            TestFactories.csvOrderItemMetadata(legacyOrderId = "1", videoId = "123")

        createOrderFromCsv.invoke(listOf(csvOrderMetadata))

        val createdOrders = ordersRepository.findAll()
        assertThat(createdOrders).hasSize(1)
        assertThat(createdOrders.first().id.value).isEqualTo(orderToBeCreated.id.value)
    }
}
