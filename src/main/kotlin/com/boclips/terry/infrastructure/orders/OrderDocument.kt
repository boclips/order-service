package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
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
    val items: List<OrderItemDocument>
) {
    fun toOrder(): Order =
        Order(
            id = OrderId(value = id.toHexString()),
            uuid = uuid,
            creatorEmail = creatorEmail,
            vendorEmail = vendorEmail,
            status = OrderStatus.parse(status),
            isbnOrProductNumber = isbnOrProductNumber,
            updatedAt = updatedAt,
            createdAt = createdAt,
            items = items.map {
                OrderItem(
                    uuid = it.uuid,
                    price = it.price,
                    transcriptRequested = it.transcriptRequested
                )
            }
        )
}