package com.boclips.terry.application.orders.converters

import com.boclips.terry.application.orders.converters.metadataConverters.FulfilmentDateFieldConverter
import com.boclips.terry.application.orders.converters.metadataConverters.OrderItemsFieldConverter
import com.boclips.terry.application.orders.converters.metadataConverters.RequestDateFieldConverter
import com.boclips.terry.application.orders.exceptions.InvalidCsvException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class CsvOrderConverter(
    val orderItemsFieldConverter: OrderItemsFieldConverter
) {
    fun toOrders(csvOrderItems: List<CsvOrderItemMetadata>): List<Order> {
        return csvOrderItems
            .groupBy {
                it.legacyOrderId
            }.mapNotNull {
                try {
                    Order(
                        id = OrderId(ObjectId().toHexString()),
                        legacyOrderId = it.key,
                        status = OrderStatus.COMPLETED,
                        createdAt = RequestDateFieldConverter.convert(it.value),
                        updatedAt = FulfilmentDateFieldConverter.convert(it.value),
                        isbnOrProductNumber = it.value.first().isbnProductNumber,
                        items = orderItemsFieldConverter.convert(it.value),
                        requestingUser = OrderUser.BasicUser(it.value.first().memberRequest),
                        authorisingUser = OrderUser.BasicUser(it.value.first().memberAuthorise),
                        organisation = OrderOrganisation(name = it.value.first().publisher)
                    )
                } catch (ex: InvalidCsvException) {
                    return@mapNotNull null
                }

            }
    }
}
