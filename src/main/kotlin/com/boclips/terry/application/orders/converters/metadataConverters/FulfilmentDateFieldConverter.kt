package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidFulfilmentDateException
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class FulfilmentDateFieldConverter {
    fun convert(items: List<CsvOrderItemMetadata>): Instant {
        return items.first().let {
            it.fulfilmentDate?.toInstant()
                ?: it.requestDate?.toInstant()
                ?: throw InvalidFulfilmentDateException("Could not determine fulfilment date for order: ${it.legacyOrderId}")
        }
    }
}