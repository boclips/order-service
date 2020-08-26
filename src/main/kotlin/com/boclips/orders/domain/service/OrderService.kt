package com.boclips.orders.domain.service

import com.boclips.orders.application.orders.IllegalOrderStateExport
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.*
import com.boclips.orders.domain.model.orderItem.OrderItemStatus
import com.boclips.orders.domain.service.currency.FxRateService
import com.boclips.videos.api.httpclient.VideosClient
import mu.KLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Component
class OrderService(
    val ordersRepository: OrdersRepository,
    val manifestConverter: ManifestConverter,
    val fxRateService: FxRateService,
    val videosClient: VideosClient
) {
    companion object : KLogging()

    fun createIfNonExistent(order: Order): Order {
        var retrievedOrder = ordersRepository.findOneByLegacyId(order.legacyOrderId)
        if (retrievedOrder == null) {
            try {
                order.items.forEach { videosClient.requestVideoCaptions(it.video.videoServiceId.value) }
            } catch (e: Exception) {
                logger.warn { "Could not request transcripts because ${e.message}. The order will be processed as usual." }
            }
            retrievedOrder = ordersRepository.save(order)
        }

        return updateStatus(orderId = retrievedOrder.id)
    }

    fun exportManifest(fxRatesAgainstPound: Map<Currency, BigDecimal>): Manifest = ordersRepository.findAll()
        .filter { it.status != OrderStatus.CANCELLED }
        .onEach {
            if (it.status == OrderStatus.INVALID) {
                throw IllegalOrderStateExport(it)
            }
        }
        .let { manifestConverter.toManifest(fxRatesAgainstPound, *it.toTypedArray()) }

    fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        val order = ordersRepository.update(orderUpdateCommand)

        return updateStatus(orderId = order.id)
    }

    fun updateCurrency(orderId: OrderId, currency: Currency): Order {
        val order = ordersRepository.findOne(orderId) ?: throw OrderNotFoundException(orderId)

        return update(
            OrderUpdateCommand.UpdateOrderCurrency(
                orderId,
                currency,
                fxRateService.getRate(
                    from = currency,
                    to = Currency.getInstance("GBP"),
                    on = ZonedDateTime.ofInstant(order.createdAt, ZoneId.of("UTC")).toLocalDate()
                )
            )
        )
    }

    fun bulkUpdate(commands: List<OrderUpdateCommand>): List<Order> {
        return commands.map { update(it) }
    }

    private fun updateStatus(orderId: OrderId): Order {
        val order = ordersRepository.findOne(orderId) ?: throw IllegalStateException("Cannot find order to update")
        val currentStatus = order.status

        if (order.status == OrderStatus.CANCELLED) {
            return order
        }

        return when {
            orderIsReady(order) && currentStatus != OrderStatus.READY -> ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = orderId,
                    orderStatus = OrderStatus.READY
                )
            )
            orderIsInProgress(order) && currentStatus != OrderStatus.IN_PROGRESS -> ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = orderId,
                    orderStatus = OrderStatus.IN_PROGRESS
                )
            )
            orderIsIncomplete(order) && currentStatus != OrderStatus.INCOMPLETED -> ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = orderId,
                    orderStatus = OrderStatus.INCOMPLETED
                )
            )
            else -> order
        }
    }

    private fun orderIsReady(order: Order) =
        order.currency != null && order.items.all { it.status == OrderItemStatus.READY }

    private fun orderIsIncomplete(order: Order) = order.currency == null || order.items.any { it.status == OrderItemStatus.INCOMPLETED }

    private fun orderIsInProgress(order: Order) =
        order.currency != null &&
            order.items.any { it.status != OrderItemStatus.INCOMPLETED } &&
            order.items.any {
                it.status == OrderItemStatus.IN_PROGRESS
            }
}
