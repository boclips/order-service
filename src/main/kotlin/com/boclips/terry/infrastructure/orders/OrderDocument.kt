package com.boclips.terry.infrastructure.orders

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant

data class OrderDocument(
    @BsonId val id: ObjectId,
    val orderProviderId: String,
    val status: String,
    val authorisingUser: OrderUserDocument,
    val requestingUser: OrderUserDocument,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String,
    val items: List<OrderItemDocument>?
)
