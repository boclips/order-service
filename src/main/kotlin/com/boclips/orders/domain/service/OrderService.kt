package com.boclips.orders.domain.service

import com.boclips.orders.application.orders.IllegalOrderStateExport
import com.boclips.orders.domain.exceptions.StatusUpdateNotAllowedException
import com.boclips.orders.domain.model.Manifest
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.OrderItemStatus
import com.boclips.videos.api.httpclient.VideosClient
import mu.KLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.Currency

@Component
class OrderService(
    val ordersRepository: OrdersRepository,
    val manifestConverter: ManifestConverter,
    val videosClient: VideosClient
) {
    companion object : KLogging()

    fun createIfNonExistent(order: Order): Order {
        var retrievedOrder = ordersRepository.findOneByLegacyId(order.legacyOrderId!!)
        if (retrievedOrder == null) {
            retrievedOrder = ordersRepository.save(order)
                .let { requestCaptions(it) }
        }

        return syncStatus(retrievedOrder)
    }

    fun create(order: Order): Order {
        return ordersRepository.save(order)
            .also { requestCaptions(it) }
            .also { syncStatus(it) }
    }

    fun exportManifest(fxRatesAgainstPound: Map<Currency, BigDecimal>): Manifest = ordersRepository.findAll()
        .filter { it.status != OrderStatus.CANCELLED }
        .onEach {
            if (it.status == OrderStatus.INVALID) {
                throw IllegalOrderStateExport(it)
            }
        }
        .let { manifestConverter.toManifest(fxRatesAgainstPound, *it.toTypedArray()) }

    fun bulkUpdate(commands: List<OrderUpdateCommand>): List<Order> {
        return commands.map { update(it) }
    }

    fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        validateUpdate(orderUpdateCommand)
        val order = ordersRepository.update(orderUpdateCommand)
        return syncStatus(order)
    }

    private fun validateUpdate(orderUpdateCommand: OrderUpdateCommand) {
        when (orderUpdateCommand) {
            is OrderUpdateCommand.ReplaceStatus -> validateReplaceStatus(orderUpdateCommand)
            else -> return
        }
    }

    private fun validateReplaceStatus(command: OrderUpdateCommand.ReplaceStatus) {
        val currentStatus = ordersRepository.findOne(command.orderId)?.status
        val newStatus = command.orderStatus
        when {
            cancellingOrder(newStatus) -> return
            deliveringReadyOrder(currentStatus, newStatus) -> return
            undoDeliveredOrder(currentStatus, newStatus) -> return
            else -> throw StatusUpdateNotAllowedException(
                from = currentStatus,
                to = newStatus
            )
        }
    }

    private fun cancellingOrder(newStatus: OrderStatus) = newStatus === OrderStatus.CANCELLED

    private fun deliveringReadyOrder(currentStatus: OrderStatus?, newStatus: OrderStatus) =
        currentStatus === OrderStatus.READY && newStatus === OrderStatus.DELIVERED

    private fun undoDeliveredOrder(currentStatus: OrderStatus?, newStatus: OrderStatus) =
        currentStatus === OrderStatus.DELIVERED && newStatus === OrderStatus.READY

    private fun syncStatus(order: Order): Order {
        val currentStatus = order.status

        val candidateStatus = when {
            orderIsCancelled(order) -> OrderStatus.CANCELLED
            orderIsDelivered(order) -> OrderStatus.DELIVERED
            orderIsReady(order) -> OrderStatus.READY
            orderIsIncomplete(order) -> OrderStatus.INCOMPLETED
            orderIsInProgress(order) -> OrderStatus.IN_PROGRESS
            else -> currentStatus
        }

        return if (candidateStatus != currentStatus) {
            ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = order.id,
                    orderStatus = candidateStatus
                )
            )
        } else {
            order
        }
    }

    private fun orderIsCancelled(order: Order) =
        order.status == OrderStatus.CANCELLED

    private fun orderIsDelivered(order: Order) =
        order.status == OrderStatus.DELIVERED

    private fun orderIsReady(order: Order) =
        order.currency != null &&
            order.items.all { it.status == OrderItemStatus.READY }

    private fun orderIsIncomplete(order: Order) =
        order.currency == null ||
            order.items.any { it.status == OrderItemStatus.INCOMPLETED }

    private fun orderIsInProgress(order: Order) =
        order.currency != null &&
            order.items.any { it.status == OrderItemStatus.IN_PROGRESS }

    private fun requestCaptions(order: Order): Order {
        val updateCommands =
            order.items
                .filter { orderItem -> orderItem.captionsRequested }
                .mapNotNull {
                    try {
                        videosClient.requestVideoCaptions(it.video.videoServiceId.value)
                        return@mapNotNull OrderUpdateCommand.OrderItemUpdateCommand.UpdateCaptionStatus(
                            order.id,
                            it.id,
                            AssetStatus.REQUESTED
                        )
                    } catch (e: Exception) {
                        logger.warn { "Could not request transcripts because ${e.message} for order: ${order.id.value}  - item: ${it.id}. The order will be processed as usual." }
                        return@mapNotNull null
                    }
                }

        bulkUpdate(updateCommands)
        return ordersRepository.findOne(order.id)!!
    }
}
