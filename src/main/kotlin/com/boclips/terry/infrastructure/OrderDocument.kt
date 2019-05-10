package com.boclips.terry.infrastructure

import org.bson.types.ObjectId

data class OrderDocument(
    val orderId: ObjectId,
    val legacyDocument: LegacyOrderDocument
)
