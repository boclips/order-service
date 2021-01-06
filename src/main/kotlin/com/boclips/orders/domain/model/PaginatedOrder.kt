package com.boclips.orders.domain.model

data class PaginatedOrder(
    val orders: List<Order>,
    val totalElements: Int
)
