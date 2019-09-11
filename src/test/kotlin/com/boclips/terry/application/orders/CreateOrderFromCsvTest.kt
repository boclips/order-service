package com.boclips.terry.application.orders

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories

class CreateOrderFromCsvTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var createOrderFromCsv: CreateOrderFromCsv

    @Test
    fun `can create an order`() {
        this.defaultVideoClientResponse()

        val csvOrderMetadata = TestFactories.csvOrderItemMetadata()

        createOrderFromCsv.invoke(listOf(csvOrderMetadata))

        val createdOrder = fakeOrdersRepository.findOneByLegacyId(csvOrderMetadata.legacyOrderId)

        assertThat(createdOrder).isNotNull
    }
}
