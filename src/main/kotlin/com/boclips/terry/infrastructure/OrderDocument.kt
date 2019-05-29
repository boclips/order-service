package com.boclips.terry.infrastructure

import com.boclips.terry.domain.Order
import com.boclips.terry.domain.OrderStatus
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
    val legacyDocument: LegacyOrderDocument
) {
    fun toOrder(): Order =
        Order(
            id = id.toHexString(),
            uuid = uuid,
            creatorEmail = creatorEmail,
            vendorEmail = vendorEmail,
            status = OrderStatus.parse(status),
            isbnOrProductNumber = isbnOrProductNumber,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
}
