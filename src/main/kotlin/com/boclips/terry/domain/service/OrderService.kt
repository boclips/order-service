package com.boclips.terry.domain.service

import com.boclips.terry.application.orders.IllegalOrderStateExport
import com.boclips.terry.domain.model.Manifest
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component

@Component
class OrderService(val ordersRepository: OrdersRepository) {

    fun createIfNonExistent(order: Order) {
        val retrievedOrder = ordersRepository.findOneByLegacyId(order.legacyOrderId) ?: ordersRepository.save(order)

        updateStatus(orderId = retrievedOrder.id)
    }

    fun exportManifest(): Manifest = ordersRepository.findAll()
        .filter { it.status != OrderStatus.CANCELLED }
        .onEach {
            if (it.status == OrderStatus.INCOMPLETED || it.status == OrderStatus.INVALID) {
                throw IllegalOrderStateExport(it)
            }
        }
        .let { Manifest.from(*it.toTypedArray()) }

    fun update(orderUpdateCommand: OrderUpdateCommand): Order {
        val order = ordersRepository.update(orderUpdateCommand)

        return updateStatus(orderId = order.id)
    }

    fun convertCurrency(order: Order): Order {

    }

    private fun updateStatus(orderId: OrderId): Order {
        val order = ordersRepository.findOne(orderId) ?: throw IllegalStateException("Cannot find order to update")

        if (order.status == OrderStatus.CANCELLED) {
            return order
        }

        return when {
            orderIsComplete(order) -> ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = orderId,
                    orderStatus = OrderStatus.COMPLETED
                )
            )
            order.status != OrderStatus.INCOMPLETED -> ordersRepository.update(
                OrderUpdateCommand.ReplaceStatus(
                    orderId = orderId,
                    orderStatus = OrderStatus.INCOMPLETED
                )
            )
            else -> order
        }
    }

    private fun orderIsComplete(order: Order) =
        order.currency != null && order.items.all { it.price.amount != null }
}
