package com.boclips.terry.infrastructure

import com.boclips.terry.domain.OrderStatus
import org.bson.types.ObjectId
import java.time.Instant

data class OrderDocument(
    val id: ObjectId,
    val uuid: String,
    val status: OrderStatus,
    val vendor: String,
    val creator: String,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String,
    val legacyDocument: LegacyOrderDocument
)
