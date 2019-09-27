package com.boclips.terry.domain.service

import com.boclips.terry.application.orders.IllegalOrderStateExport
import com.boclips.terry.domain.model.Manifest
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrdersRepository
import org.springframework.stereotype.Component

@Component
class OrderService(val ordersRepository: OrdersRepository) {

    fun createIfNonExistent(order: Order) {
        ordersRepository.findOneByLegacyId(order.legacyOrderId) ?: ordersRepository.save(order)
    }

    fun exportManifest() : Manifest = ordersRepository.findAll()
        .filter { it.status != OrderStatus.CANCELLED }
        .onEach {
            if (it.status == OrderStatus.INCOMPLETED || it.status == OrderStatus.INVALID) {
                throw IllegalOrderStateExport(it)
            }
        }
        .let { Manifest.from(*it.toTypedArray()) }
}
