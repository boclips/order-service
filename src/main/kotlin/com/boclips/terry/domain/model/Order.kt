package com.boclips.terry.domain.model

import com.boclips.terry.domain.model.orderItem.OrderItem
import java.time.Instant

data class Order(
    val id: OrderId,
    val orderProviderId: String,
    val status: OrderStatus,
    val authorisingUser: OrderUser,
    val requestingUser: OrderUser,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String,
    val items: List<OrderItem>
)

