package com.boclips.terry.application.orders

import com.boclips.terry.application.exceptions.InvalidCsvException
import com.boclips.terry.application.orders.converters.CsvOrderConverter
import com.boclips.terry.application.orders.converters.Errors
import com.boclips.terry.application.orders.converters.Orders
import com.boclips.terry.common.Do
import com.boclips.terry.domain.service.OrderService
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.springframework.stereotype.Component

@Component
class CreateOrderFromCsv(
    private val orderService: OrderService,
    private val orderConverter: CsvOrderConverter
) {
    fun invoke(csvOrderItems: List<CsvOrderItemMetadata>) {
        val ordersResult = orderConverter.toOrders(csvOrderItems)
        Do exhaustive when (ordersResult) {
            is Orders -> ordersResult.orders.forEach { orderService.createIfNonExistent(it) }
            is Errors -> throw InvalidCsvException(errors = ordersResult)
        }
    }
}
