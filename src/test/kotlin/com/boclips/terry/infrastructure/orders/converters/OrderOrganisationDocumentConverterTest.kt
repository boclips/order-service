package com.boclips.terry.infrastructure.orders.converters

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class OrderOrganisationDocumentConverterTest {
    @Test
    fun `converts an orderUser to document to orderUser`() {
        val originalModel = TestFactories.orderOrganisation(
            sourceOrganisationId = "hello123",
            name = "Publicher Name"
        )

        val convertedDocument = OrderOrganisationDocumentConverter.toOrderOrganisationDocument(originalModel)
        val reconvertedModel = OrderOrganisationDocumentConverter.toOrderOrganisation(convertedDocument)

        Assertions.assertThat(reconvertedModel).isEqualTo(originalModel)
    }
}
