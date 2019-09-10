package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidMetadataItemsException
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component

@Component
class OrderItemsFieldConverter {
    fun convert(items: List<CsvOrderItemMetadata>): List<OrderItem> {
        return items.map { OrderItemFieldConverter().convert(it) }.let {
            if (it.isEmpty()) {
                throw InvalidMetadataItemsException("No valid items")
            } else {
                it
            }
        }
    }
}
