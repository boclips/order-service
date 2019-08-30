package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.infrastructure.orders.OrderOrganisationDocument

object OrderOrganisationDocumentConverter {

    fun toOrderOrganisationDocument(orderOrganisation: OrderOrganisation): OrderOrganisationDocument {
        return OrderOrganisationDocument(
            legacyOrganisationId = orderOrganisation.legacyOrganisationId,
            name = orderOrganisation.name
        )
    }

    fun toOrderOrganisation(orderOrganisationDocument: OrderOrganisationDocument): OrderOrganisation {
        return OrderOrganisation(
            legacyOrganisationId = orderOrganisationDocument.legacyOrganisationId,
            name = orderOrganisationDocument.name
        )
    }
}
