package com.boclips.orders.application.orders

import com.boclips.orders.application.exceptions.InvalidCsvException
import com.boclips.orders.application.orders.converters.csv.CsvOrderConverter
import com.boclips.orders.application.orders.converters.csv.Errors
import com.boclips.orders.application.orders.converters.csv.Orders
import com.boclips.orders.common.Do
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.presentation.orders.CsvOrderItemMetadata
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
