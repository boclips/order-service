package com.boclips.terry.domain

interface LegacyOrdersRepository {
    fun add(order: LegacyOrder)
    fun findAll(): List<LegacyOrder>
}
