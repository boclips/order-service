package com.boclips.terry.application.orders

import com.boclips.terry.application.orders.converters.CsvOrderConverter
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component

@Component
class CreateOrderFromCsv(
    private val ordersRepository: OrdersRepository,
    private val orderConverter: CsvOrderConverter
) {
    fun invoke(csvOrderItems: List<CsvOrderItemMetadata>) {
        val orders = orderConverter.toOrders(csvOrderItems)

        orders.map { ordersRepository.add(it) }
    }
}
