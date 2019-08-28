package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.infrastructure.orders.OrderOrganisationDocument

object OrderOrganisationDocumentConverter {

    fun toOrderOrganisationDocument(orderOrganisation: OrderOrganisation): OrderOrganisationDocument {
        return OrderOrganisationDocument(
            sourceOrganisationId = orderOrganisation.sourceOrganisationId,
            name = orderOrganisation.name
        )
    }

    fun toOrderOrganisation(orderOrganisationDocument: OrderOrganisationDocument): OrderOrganisation {
        return OrderOrganisation(
            sourceOrganisationId = orderOrganisationDocument.sourceOrganisationId,
            name = orderOrganisationDocument.name
        )
    }
}
