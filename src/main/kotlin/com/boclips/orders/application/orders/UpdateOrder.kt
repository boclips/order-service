package com.boclips.orders.application.orders

import com.boclips.orders.application.orders.exceptions.InvalidCurrencyFormatException
import com.boclips.orders.application.orders.exceptions.InvalidOrderUpdateRequest
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.domain.service.currency.FxRateService
import com.boclips.orders.presentation.UpdateOrderRequest
import com.boclips.orders.presentation.orders.OrderResource
import com.boclips.orders.presentation.orders.OrderStatusResource
import org.springframework.stereotype.Component
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
            }
        )

        if (commands.isEmpty()) {
            throw InvalidOrderUpdateRequest("No valid fields specified")
        }

        orderService.bulkUpdate(commands)
        return ordersRepository.findOne(orderId)?.let { OrderResource.fromOrder(it) } ?: throw OrderNotFoundException(
            orderId
        )
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
        it: OrderStatusResource,
        orderId: OrderId
    ): OrderUpdateCommand.ReplaceStatus {

        return OrderUpdateCommand.ReplaceStatus(
            orderId = orderId,
            orderStatus = when (it) {
                OrderStatusResource.DELIVERED -> OrderStatus.DELIVERED
                OrderStatusResource.READY -> TODO()
                OrderStatusResource.IN_PROGRESS -> TODO()
                OrderStatusResource.INCOMPLETED -> TODO()
                OrderStatusResource.CANCELLED -> TODO()
                OrderStatusResource.INVALID -> TODO()
            }
        )
    }
}


