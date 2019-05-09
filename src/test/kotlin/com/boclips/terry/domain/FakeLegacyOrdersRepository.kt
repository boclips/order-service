package com.boclips.terry.domain

class FakeLegacyOrdersRepository : LegacyOrdersRepository {
    lateinit var legacyOrders: MutableList<LegacyOrder>

    init {
        clear()
    }

    override fun add(item: LegacyOrder) = this.also { legacyOrders.add(item) }
    override fun clear() = this.also { legacyOrders = mutableListOf() }
    override fun findAll(): List<LegacyOrder> = legacyOrders
}
