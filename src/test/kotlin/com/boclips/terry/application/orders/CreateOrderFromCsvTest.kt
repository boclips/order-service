package com.boclips.terry.application.orders

import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class CreateOrderFromCsvTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var createOrderFromCsv: CreateOrderFromCsv

    @Test
    fun `can create an order`() {
        val csvOrderMetadata = CsvOrderItemMetadata()

        createOrderFromCsv.invoke(listOf(csvOrderMetadata))

        val createdOrder = fakeOrdersRepository.findOneByLegacyId(csvOrderMetadata.legacyOrderId)

        assertThat(createdOrder).isNotNull
    }
}