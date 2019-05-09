package com.boclips.terry.domain

class FakeOrdersRepository : OrdersRepository {
    lateinit var orders: MutableList<Order>

    init {
        clear()
    }

    override fun clear(): OrdersRepository = this.also {
        orders = mutableListOf()
    }

    override fun add(item: Order) = this.also {
        orders.add(item)
    }

    override fun findAll(): List<Order> = orders
}
