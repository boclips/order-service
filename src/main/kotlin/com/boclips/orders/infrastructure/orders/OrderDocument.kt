package com.boclips.orders.infrastructure.orders

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

data class OrderDocument(
    @BsonId val id: ObjectId,
    val legacyOrderId: String,
    val cancelled: Boolean,
    val authorisingUser: OrderUserDocument? = null,
    val requestingUser: OrderUserDocument,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String?,
    val items: List<OrderItemDocument>?,
    val organisation: String? = null,
    val currency: Currency?,
    val orderThroughPlatform: Boolean,
    val fxRateToGbp: BigDecimal?
)
