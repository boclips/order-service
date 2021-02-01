package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.exceptions.StatusUpdateNotAllowedException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.domain.service.currency.FxRateService
import com.boclips.orders.presentation.UpdateOrderRequest
import com.boclips.orders.presentation.UpdateOrderStatusRequest
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Currency

@Component
class UpdateOrder(
    private val orderService: OrderService,
    private val fxRateService: FxRateService,
    private val ordersRepository: OrdersRepository
) {
    operator fun invoke(id: String, updateOrderRequest: UpdateOrderRequest?): OrderResource {
        val orderId = OrderId(id)

        ordersRepository.findOne(orderId) ?: throw OrderNotFoundException(orderId)

        val commands = listOfNotNull(
            updateOrderRequest?.organisation?.let {
                getOrganisationUpdate(it, orderId)
            },
            updateOrderRequest?.currency?.let {
                getCurrencyUpdate(it, orderId)
            },
            updateOrderRequest?.status?.let {
                getStatusUpdate(it, orderId)
            },
            updateOrderRequest?.status?.let {
                getDeliveredAtUpdate(orderId, it)
            }
        )

        if (commands.isEmpty()) {
            throw InvalidOrderUpdateRequest("No valid fields specified")
        }

        try {
            orderService.bulkUpdate(commands)
        } catch (e: StatusUpdateNotAllowedException) {
            throw InvalidOrderUpdateRequest("Cannot update status from ${e.from} to ${e.to}")
        }

        return ordersRepository.findOne(orderId)
            ?.let { OrderResource.fromOrder(it) }
            ?: throw OrderNotFoundException(orderId)
    }

    private fun getCurrencyUpdate(
        it: String,
        orderId: OrderId
    ): OrderUpdateCommand.UpdateOrderCurrency {
        validateCurrency(it)

        val currency = Currency.getInstance(it)
        val order = ordersRepository.findOne(orderId) ?: throw OrderNotFoundException(orderId)

        return OrderUpdateCommand.UpdateOrderCurrency(
            orderId = orderId,
            currency = currency,
            fxRateToGbp = fxRateService.getRate(
                from = currency,
                to = Currency.getInstance("GBP"),
                on = ZonedDateTime.ofInstant(order.createdAt, ZoneId.of("UTC")).toLocalDate()
            )
        )
    }

    private fun getOrganisationUpdate(
        it: String,
        orderId: OrderId
    ): OrderUpdateCommand.UpdateOrderOrganisation {
        if (it.isBlank()) {
            throw InvalidOrderUpdateRequest("Organisation must not be blank")
        }

        return OrderUpdateCommand.UpdateOrderOrganisation(
            orderId = orderId,
            organisation = OrderOrganisation(name = it)
        )
    }

    private fun validateCurrency(currency: String) {
        (Currency.getAvailableCurrencies()
            .firstOrNull { it.currencyCode == currency }
            ?: throw InvalidCurrencyFormatException(currency))
    }

    private fun getStatusUpdate(
        it: UpdateOrderStatusRequest,
        orderId: OrderId
    ) = OrderUpdateCommand.ReplaceStatus(
        orderId = orderId,
        orderStatus = when (it) {
            UpdateOrderStatusRequest.DELIVERED -> OrderStatus.DELIVERED
            UpdateOrderStatusRequest.READY -> OrderStatus.READY
        }
    )

    private fun getDeliveredAtUpdate(
        orderId: OrderId,
        it: UpdateOrderStatusRequest
    ) = OrderUpdateCommand.ReplaceDeliveredAt(
        orderId = orderId, deliveredAt = when (it) {
            UpdateOrderStatusRequest.DELIVERED -> Instant.now()
            UpdateOrderStatusRequest.READY -> null
        }
    )
}


