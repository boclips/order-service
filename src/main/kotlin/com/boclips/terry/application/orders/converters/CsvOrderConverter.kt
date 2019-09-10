package com.boclips.terry.application.orders.converters

import com.boclips.terry.application.orders.converters.metadataConverters.FulfilmentDateFieldConverter
import com.boclips.terry.application.orders.converters.metadataConverters.OrderItemsFieldConverter
import com.boclips.terry.application.orders.converters.metadataConverters.RequestDateFieldConverter
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
    val requestDateFieldConverter: RequestDateFieldConverter,
    val fulfilmentDateFieldConverter: FulfilmentDateFieldConverter,
    val orderItemsFieldConverter: OrderItemsFieldConverter
) {
    fun toOrders(csvOrderItemMetadatas: List<CsvOrderItemMetadata>): List<Order> {
        return csvOrderItemMetadatas
            .groupBy {
                it.legacyOrderId
            }.mapNotNull {
                try {
                    Order(
                        id = OrderId(ObjectId().toHexString()),
                        legacyOrderId = it.key,
                        status = OrderStatus.COMPLETED,
                        createdAt = requestDateFieldConverter.convert(it.value),
                        updatedAt = fulfilmentDateFieldConverter.convert(it.value),
                        isbnOrProductNumber = it.value.first().isbnProductNumber,
                        items = orderItemsFieldConverter.convert(it.value),
                        requestingUser = OrderUser.BasicUser(it.value.first().memberRequest),
                        authorisingUser = OrderUser.BasicUser(it.value.first().memberAuthorise),
                        organisation = OrderOrganisation(name = it.value.first().publisher)
                    )
                } catch (ex: Exception) {
                    return@mapNotNull null
                }

            }
    }
}
