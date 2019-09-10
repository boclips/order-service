package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidRequestDateException
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class RequestDateFieldConverter {
    fun convert(items: List<CsvOrderItemMetadata>): Instant {
        return items.first().requestDate?.toInstant()
            ?: throw InvalidRequestDateException("Invalid request date for legacy order id :${items.firstOrNull()?.legacyOrderId}")
    }
}
