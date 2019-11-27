package com.boclips.orders.domain.model

import com.boclips.orders.infrastructure.orders.LegacyOrderDocument

interface LegacyOrdersRepository {
    fun add(document: LegacyOrderDocument): LegacyOrdersRepository
    fun findAll(): List<LegacyOrderDocument>
    fun clear(): LegacyOrdersRepository
}
