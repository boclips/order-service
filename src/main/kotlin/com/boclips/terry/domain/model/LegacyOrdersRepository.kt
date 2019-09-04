package com.boclips.terry.domain.model

import com.boclips.terry.infrastructure.orders.LegacyOrderDocument

interface LegacyOrdersRepository {
    fun add(document: LegacyOrderDocument): LegacyOrdersRepository
    fun findAll(): List<LegacyOrderDocument>
    fun clear(): LegacyOrdersRepository
}
