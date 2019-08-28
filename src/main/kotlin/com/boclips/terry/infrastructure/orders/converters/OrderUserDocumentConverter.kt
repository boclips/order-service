package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.infrastructure.orders.OrderUserDocument

object OrderUserDocumentConverter {
    fun toOrderUserDocument(orderUser: OrderUser): OrderUserDocument {
        return OrderUserDocument(
            firstName = orderUser.firstName,
            lastName = orderUser.lastName,
            email = orderUser.email,
            sourceUserId = orderUser.sourceUserId,
            organisation = OrderOrganisationDocumentConverter.toOrderOrganisationDocument(orderUser.organisation)
        )
    }

    fun toOrderUser(orderUserDocument: OrderUserDocument): OrderUser {
        return OrderUser(
            firstName = orderUserDocument.firstName,
            lastName = orderUserDocument.lastName,
            email = orderUserDocument.email,
            sourceUserId = orderUserDocument.sourceUserId,
            organisation = OrderOrganisationDocumentConverter.toOrderOrganisation(orderUserDocument.organisation)
        )
    }
}
