package com.boclips.orders.application.orders

import com.boclips.orders.common.PageInfo
import com.boclips.orders.common.PageRequest
import com.boclips.orders.common.ResultsPage
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.presentation.orders.OrderResource
import org.springframework.stereotype.Component

@Component
class GetOrders(
    private val orderRepository: OrdersRepository
) {
    fun getPaginated(pageSize: Int, pageNumber: Int, userId: String): ResultsPage<Order, Int> {

        val orders = orderRepository.getPaginated(pageSize, pageNumber, userId)

        val totalElements = orderRepository.findAll().size

        return ResultsPage(
            elements = orders,
            counts = totalElements,
            pageInfo = PageInfo(
                hasMoreElements = (pageNumber + 1) * pageSize < totalElements,
                totalElements = totalElements.toLong(),
                pageRequest = PageRequest(page = pageNumber, size = pageSize)
            )
        )
    }

    fun getAll() = orderRepository.findAll()
        .map { OrderResource.fromOrder(it) }
}
