package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.infrastructure.orders.OrderUserDocument

object OrderUserDocumentConverter {
    fun toOrderUserDocument(orderUser: OrderUser): OrderUserDocument {
        return when (orderUser) {
            is OrderUser.CompleteUser -> OrderUserDocument(
                firstName = orderUser.firstName,
                lastName = orderUser.lastName,
                email = orderUser.email,
                legacyUserId = orderUser.legacyUserId,
                organisation = OrderOrganisationDocumentConverter.toOrderOrganisationDocument(orderUser.organisation),
                label = null
            )
            is OrderUser.BasicUser -> OrderUserDocument(
                label = orderUser.label,
                firstName = null,
                lastName = null,
                email = null,
                legacyUserId = null,
                organisation = null
            )
        }
    }

    fun toOrderUser(orderUserDocument: OrderUserDocument): OrderUser {
        return if (orderUserDocument.isCompleteUser()) {
            OrderUser.CompleteUser(
                firstName = orderUserDocument.firstName!!,
                lastName = orderUserDocument.lastName!!,
                email = orderUserDocument.email!!,
                legacyUserId = orderUserDocument.legacyUserId!!,
                organisation = OrderOrganisationDocumentConverter.toOrderOrganisation(orderUserDocument.organisation!!)
            )
        } else {
            OrderUser.BasicUser(label = orderUserDocument.label!!) //TODO add a missing user
        }
    }
}
