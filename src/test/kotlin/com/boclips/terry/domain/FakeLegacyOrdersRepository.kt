package com.boclips.terry.domain

class FakeLegacyOrdersRepository : LegacyOrdersRepository {
    var legacyOrders: MutableList<LegacyOrder> = mutableListOf()

    override fun add(order: LegacyOrder) {
        legacyOrders.add(order)
    }

    override fun findAll(): List<LegacyOrder> = legacyOrders
}
