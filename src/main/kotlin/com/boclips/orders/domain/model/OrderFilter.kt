package com.boclips.orders.domain.model

sealed class OrderFilter {
     class HasStatus(vararg val status: OrderStatus) : OrderFilter()
}
