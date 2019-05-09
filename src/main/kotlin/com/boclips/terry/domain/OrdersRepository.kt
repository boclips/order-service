package com.boclips.terry.domain

interface OrdersRepository {
    fun add(item: Order): OrdersRepository
    fun clear(): OrdersRepository
    fun findAll(): List<Order>
}
