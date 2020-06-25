package com.boclips.orders.domain.model

sealed class OrderFilter {
    data class HasStatus(val status: OrderStatus) : OrderFilter()
}
