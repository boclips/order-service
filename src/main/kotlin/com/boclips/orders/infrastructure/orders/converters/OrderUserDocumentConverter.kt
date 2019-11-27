package com.boclips.orders.infrastructure.orders.converters

import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.infrastructure.orders.OrderUserDocument

object OrderUserDocumentConverter {
    fun toOrderUserDocument(orderUser: OrderUser): OrderUserDocument {
        return when (orderUser) {
            is OrderUser.CompleteUser -> OrderUserDocument(
                firstName = orderUser.firstName,
                lastName = orderUser.lastName,
                email = orderUser.email,
                legacyUserId = orderUser.legacyUserId,
                label = null
            )
            is OrderUser.BasicUser -> OrderUserDocument(
                label = orderUser.label,
                firstName = null,
                lastName = null,
                email = null,
                legacyUserId = null
            )
        }
    }

    fun toOrderUser(orderUserDocument: OrderUserDocument): OrderUser {
        return if (orderUserDocument.isCompleteUser()) {
            OrderUser.CompleteUser(
                firstName = orderUserDocument.firstName!!,
                lastName = orderUserDocument.lastName!!,
                email = orderUserDocument.email!!,
                legacyUserId = orderUserDocument.legacyUserId!!
            )
        } else {
            OrderUser.BasicUser(label = orderUserDocument.label!!)
        }
    }
}
