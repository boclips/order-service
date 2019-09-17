package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidFulfilmentDateCsvException
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import java.time.Instant

object FulfilmentDateFieldConverter {
    fun convert(items: List<CsvOrderItemMetadata>): Instant {
        return items.first().let {
            it.fulfilmentDate?.toInstant()
                ?: it.requestDate?.toInstant()
                ?: throw InvalidFulfilmentDateCsvException("Could not determine fulfilment date for order: ${it.legacyOrderId}")
        }
    }
}
