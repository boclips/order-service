package com.boclips.terry.domain

interface OrdersRepository {
    fun add(order: Order)
    fun findAll(): List<Order>
}
