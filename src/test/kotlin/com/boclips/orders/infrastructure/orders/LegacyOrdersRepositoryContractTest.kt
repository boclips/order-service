package com.boclips.orders.infrastructure.orders

import testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class LegacyOrdersRepositoryTests: AbstractSpringIntegrationTest() {

    @Test
    fun `can create a legacy order`() {
        val legacyOrder = TestFactories.legacyOrder(ObjectId().toHexString())

        val legacyOrderDocument = TestFactories.legacyOrderDocument(
            legacyOrder = legacyOrder
        )

        legacyOrdersRepository.add(legacyOrderDocument)

        val documents = legacyOrdersRepository.findAll()
        assertThat(documents).hasSize(1)
        assertThat(documents.first()).isEqualTo(legacyOrderDocument)
    }
}
