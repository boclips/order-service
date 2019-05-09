package com.boclips.terry.domain

interface LegacyOrdersRepository {
    fun add(item: LegacyOrder): LegacyOrdersRepository
    fun clear(): LegacyOrdersRepository
    fun findAll(): List<LegacyOrder>
}
