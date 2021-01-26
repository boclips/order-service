package com.boclips.orders.application.orders.converters.csv

import com.boclips.orders.application.orders.converters.parseTrimRequest
import com.boclips.orders.domain.exceptions.BoclipsException
import com.boclips.orders.domain.model.*
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.orders.presentation.orders.CsvOrderItemMetadata
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.*
import kotlin.reflect.KClass

@Component
class CsvOrderConverter(val videoProvider: VideoProvider) {
    companion object : KLogging()

    fun toOrders(csvOrderItems: List<CsvOrderItemMetadata>): OrdersResult {
        val errors = mutableListOf<OrderConversionError>()
        return csvOrderItems
            .groupBy { it.legacyOrderId }
            .mapNotNull { (legacyOrderId, csvOrderItems) ->
                logger.info { "Attempting to parse order: $legacyOrderId" }

                val validator = OrderValidator(legacyOrderId, errors)
                val firstOrderItem = csvOrderItems.first()
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

                validator.setNotNullOrError(
                    firstOrderItem.orderThroughPlatform,
                    {
                        val isThroughPlatform = it.parseBoolean()
                        orderBuilder.orderSource(if (isThroughPlatform) OrderSource.LEGACY else OrderSource.MANUAL)
                    },
                    "Field ${CsvOrderItemMetadata.ORDER_THROUGH_PLATFORM} '${firstOrderItem.orderThroughPlatform}' has an invalid format, try yes or no instead"
                )

                val orderItems = csvOrderItems.mapNotNull { toOrderItem(it, validator) }
                orderBuilder.items(orderItems)
                orderItems
                    .firstOrNull()
                    ?.let { orderBuilder.currency(it.price.currency) }

                orderBuilder
                    .takeIf { errors.isEmpty() }
                    ?.run {
                        status(OrderStatus.INCOMPLETED)
                            .isbnOrProductNumber(firstOrderItem.isbnProductNumber)
                            .authorisingUser(firstOrderItem.memberAuthorise?.let { OrderUser.BasicUser(it) })
                            .organisation(firstOrderItem.publisher?.let { OrderOrganisation(name = it) })
                            .build()
                    }
            }
            .let { orders ->
                OrdersResult.instanceOf(orders, errors)
            }
    }

    fun toOrderItem(csvItem: CsvOrderItemMetadata, validator: OrderValidator): OrderItem? {
        val orderItemBuilder = OrderItem.builder()

        validator.setNotNullOrError(
            value = csvItem.videoId?.trim(),
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

        return orderItemBuilder
            .takeIf { validator.errors.isEmpty() }
            ?.run {
                price(csvItem.price.parsePrice())
                    .captionsRequested(csvItem.captioning.parseBoolean())
                    .trim(csvItem.trim.parseTrimRequest())
                    .license(orderItemLicenseBuilder.build())
                    .notes(csvItem.notes?.takeIf { it.isNotBlank() })
                    .id(UUID
                        .randomUUID()
                        .toString())
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
    this
        .firstOrNull { (exception) -> exception.isInstance(e) }
        ?.let { (_, message) -> "$message: ${e.message}" }

