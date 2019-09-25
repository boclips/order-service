package com.boclips.terry.application.orders.converters

import com.boclips.terry.application.orders.converters.metadataConverters.OrderItemsFieldConverter
import com.boclips.terry.application.orders.converters.metadataConverters.parseCsvDate
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class CsvOrderConverter(
    val orderItemsFieldConverter: OrderItemsFieldConverter
) {
    companion object : KLogging()

    fun toOrders(csvOrderItems: List<CsvOrderItemMetadata>): OrdersResult {
        val errors = mutableListOf<OrderConversionError>()
        return csvOrderItems
            .groupBy { it.legacyOrderId }
            .mapNotNull { (legacyOrderId, orderItems) ->
                logger.info { "Attempting to parse order: $legacyOrderId" }
                fun <T> T?.setOrError(setter: (prop: T) -> Unit, error: String) {
                    this?.let { setter(it) } ?: errors.add(
                        OrderConversionError(
                            legacyOrderId = legacyOrderId,
                            message = error
                        )
                    )
                }

                val firstOrderItem = orderItems.first()
                val orderBuilder = Order.builder()

                legacyOrderId.setOrError(
                    { orderBuilder.legacyOrderId(it) },
                    "Field ${CsvOrderItemMetadata.ORDER_NO} must not be null"
                )

                firstOrderItem.requestDate.parseCsvDate().setOrError(
                    { orderBuilder.createdAt(it) },
                    "Field ${CsvOrderItemMetadata.ORDER_REQUEST_DATE} '${firstOrderItem.requestDate}' has an invalid format. Try DD/MM/YYYY instead."
                )

                firstOrderItem.fulfilmentDate.parseCsvDate(firstOrderItem.requestDate).setOrError(
                    { orderBuilder.updatedAt(it) },
                    "Field ${CsvOrderItemMetadata.ORDER_FULFILLMENT_DATE} '${firstOrderItem.fulfilmentDate}' has an invalid format. Try DD/MM/YYYY instead."
                )

                firstOrderItem.memberRequest.setOrError(
                    { orderBuilder.requestingUser(OrderUser.BasicUser(it)) },
                    "Field ${CsvOrderItemMetadata.MEMBER_REQUEST} must not be null"
                )

                orderBuilder.takeIf { errors.isEmpty() }?.run {
                    status(OrderStatus.INCOMPLETED)
                        .isbnOrProductNumber(firstOrderItem.isbnProductNumber)
                        .authorisingUser(firstOrderItem.memberAuthorise?.let { OrderUser.BasicUser(it) })
                        .organisation(firstOrderItem.publisher?.let { OrderOrganisation(name = it) })
                        .items(orderItemsFieldConverter.convert(orderItems))
                        .build()
                }
            }.let { orders ->
                OrdersResult.instanceOf(orders, errors)
            }
    }
}
