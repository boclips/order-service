package com.boclips.terry.application.orders.converters.csv

import com.boclips.terry.application.orders.converters.parseTrimRequest
import com.boclips.terry.domain.exceptions.BoclipsException
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import mu.KLogging
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class CsvOrderConverter(val videoProvider: VideoProvider) {
    companion object : KLogging()

    fun toOrders(csvOrderItems: List<CsvOrderItemMetadata>): OrdersResult {
        val errors = mutableListOf<OrderConversionError>()
        return csvOrderItems
            .groupBy { it.legacyOrderId }
            .mapNotNull { (legacyOrderId, orderItems) ->
                logger.info { "Attempting to parse order: $legacyOrderId" }

                val validator =
                    OrderValidator(legacyOrderId, errors)
                val firstOrderItem = orderItems.first()
                val orderBuilder = Order.builder()

                validator.setNotNullOrError(
                    legacyOrderId,
                    { orderBuilder.legacyOrderId(it) },
                    "Field ${CsvOrderItemMetadata.ORDER_NO} must not be null"
                )

                validator.setNotNullOrError(
                    firstOrderItem.requestDate.parseCsvDate(),
                    { orderBuilder.createdAt(it) },
                    "Field ${CsvOrderItemMetadata.ORDER_REQUEST_DATE} '${firstOrderItem.requestDate}' has an invalid format, try DD/MM/YYYY instead"
                )

                validator.setNotNullOrError(
                    firstOrderItem.fulfilmentDate.parseCsvDate(firstOrderItem.requestDate),
                    { orderBuilder.updatedAt(it) },
                    "Field ${CsvOrderItemMetadata.ORDER_FULFILLMENT_DATE} '${firstOrderItem.fulfilmentDate}' has an invalid format, try DD/MM/YYYY instead"
                )

                validator.setNotNullOrError(
                    firstOrderItem.memberRequest,
                    { orderBuilder.requestingUser(OrderUser.BasicUser(it)) },
                    "Field ${CsvOrderItemMetadata.MEMBER_REQUEST} must not be null"
                )

                orderBuilder.takeIf { errors.isEmpty() }?.run {
                    status(OrderStatus.INCOMPLETED)
                        .isbnOrProductNumber(firstOrderItem.isbnProductNumber)
                        .authorisingUser(firstOrderItem.memberAuthorise?.let { OrderUser.BasicUser(it) })
                        .organisation(firstOrderItem.publisher?.let { OrderOrganisation(name = it) })
                        .items(orderItems.mapNotNull { toOrderItem(it, validator) })
                        .build()
                }
            }.let { orders ->
                OrdersResult.instanceOf(orders, errors)
            }
    }

    fun toOrderItem(csvItem: CsvOrderItemMetadata, validator: OrderValidator): OrderItem? {
        val orderItemBuilder = OrderItem.builder()

        validator.setNotNullOrError(
            value = csvItem.videoId,
            setter = { orderItemBuilder.video(videoProvider.get(VideoId(value = it))) },
            defaultErrorMessage = "Field ${CsvOrderItemMetadata.CLIP_ID} must not be null",
            errorMessages = *arrayOf(BoclipsException::class to "${CsvOrderItemMetadata.CLIP_ID} error")
        )

        val orderItemLicenseBuilder = OrderItemLicense.builder()

        validator.setNotNullOrError(
            csvItem.licenseDuration.parseLicenseDuration(),
            { orderItemLicenseBuilder.duration(it) },
            "Field ${CsvOrderItemMetadata.LICENSE_DURATION} '${csvItem.licenseDuration}' has an invalid format, try a number or a textual description instead"
        )

        validator.setNotNullOrError(
            csvItem.territory,
            { orderItemLicenseBuilder.territory(it) },
            "Field ${CsvOrderItemMetadata.TERRITORY} must not be null"
        )

        return orderItemBuilder.takeIf { validator.errors.isEmpty() }?.run {
            price(csvItem.price.parsePrice())
                .transcriptRequested(csvItem.captioning.parseBoolean())
                .trim(csvItem.trim.parseTrimRequest())
                .license(orderItemLicenseBuilder.build())
                .notes(csvItem.notes?.takeIf { it.isNotBlank() })
                .build()
        }
    }
}

data class OrderValidator(
    val legacyOrderId: String?,
    val errors: MutableList<OrderConversionError>
) {
    companion object : KLogging()

    fun <T> setNotNullOrError(
        value: T?,
        setter: (prop: T) -> Unit,
        defaultErrorMessage: String,
        vararg errorMessages: Pair<KClass<out Exception>, String>
    ) {
        if (value == null) {
            addError(defaultErrorMessage)
        } else {
            try {
                setter(value)
            } catch (e: Exception) {
                logger.info("Could not set value $value for legacy order id: $legacyOrderId", e)

                addError(
                    errorMessages.findMessageForException(e) ?: defaultErrorMessage
                )
            }
        }
    }

    private fun addError(defaultErrorMessage: String) {
        errors.add(
            OrderConversionError(
                legacyOrderId = legacyOrderId,
                message = defaultErrorMessage
            )
        )
    }
}

private fun Array<out Pair<KClass<out Exception>, String>>.findMessageForException(e: Exception) =
    this.firstOrNull { (exception) -> exception.isInstance(e) }
        ?.let { (_, message) -> "$message: ${e.message}" }

