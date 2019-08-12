package com.boclips.terry.infrastructure.orders

import org.bson.types.ObjectId
import java.time.Instant

data class OrderDocument(
    val id: ObjectId,
    val uuid: String,
    val status: String,
    val vendorEmail: String,
    val creatorEmail: String,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String,
    val items: List<OrderItemDocument>?
)
