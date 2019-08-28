package com.boclips.terry.infrastructure.orders.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class OrderUserDocumentConverterTest {
    @Test
    fun `converts an orderUser to document to orderUser`() {
        val originalModel = TestFactories.orderUser(
            firstName = "Bob",
            lastName = "Smith",
            email = "test@test.com",
            sourceUserId = "abc"
        )

        val convertedDocument = OrderUserDocumentConverter.toOrderUserDocument(originalModel)
        val reconvertedModel = OrderUserDocumentConverter.toOrderUser(convertedDocument)

        assertThat(reconvertedModel).isEqualTo(originalModel)
    }
}
