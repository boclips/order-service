package com.boclips.terry.domain

class FakeOrdersRepository : OrdersRepository {
    var orders: MutableList<Order> = mutableListOf()

    override fun add(order: Order) {
        orders.add(order)
    }

    override fun findAll(): List<Order> = orders
}
