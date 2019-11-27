package com.boclips.orders.infrastructure.orders.converters

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.OrderFactory

class OrderUserDocumentConverterTest {
    @Test
    fun `converts an orderUser to document to orderUser`() {
        val originalModel = OrderFactory.completeOrderUser(
            firstName = "Bob",
            lastName = "Smith",
            email = "test@test.com",
            sourceUserId = "abc"
        )

        val convertedDocument = OrderUserDocumentConverter.toOrderUserDocument(originalModel)
        val reconvertedModel = OrderUserDocumentConverter.toOrderUser(convertedDocument)

        assertThat(reconvertedModel).isEqualTo(originalModel)
    }

    @Test
    fun `converts an complete user to document and back to order user`() {
        val completeUser = OrderFactory.completeOrderUser()
        val convertedDocument = OrderUserDocumentConverter.toOrderUserDocument(completeUser)
        val reconvertedModel = OrderUserDocumentConverter.toOrderUser(convertedDocument)

        assertThat(reconvertedModel).isEqualTo(completeUser)
    }

    @Test
    fun `converts a basic user to document and back to order user`() {
        val basicUser = OrderFactory.basicOrderUser()
        val convertedDocument = OrderUserDocumentConverter.toOrderUserDocument(basicUser)
        val reconvertedModel = OrderUserDocumentConverter.toOrderUser(convertedDocument)

        assertThat(reconvertedModel).isEqualTo(basicUser)
    }
}
