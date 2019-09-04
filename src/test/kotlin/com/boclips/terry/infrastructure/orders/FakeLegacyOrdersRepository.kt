package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.LegacyOrdersRepository

class FakeLegacyOrdersRepository : LegacyOrdersRepository {
    lateinit var documents: MutableList<LegacyOrderDocument>

    init {
        clear()
    }

    override fun clear(): LegacyOrdersRepository = this.also {
        documents = mutableListOf()
    }

    override fun add(document: LegacyOrderDocument) = this.also {
        documents.add(document)
    }

    override fun findAll(): List<LegacyOrderDocument> {
        return documents
    }
}
