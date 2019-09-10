package com.boclips.terry.application.orders

import com.boclips.terry.application.orders.converters.CsvOrderConverter
import com.boclips.terry.application.orders.converters.OrderStatusConverter
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.presentation.resources.CsvOrderItemMetadata
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateOrderFromCsv(private val ordersRepository: OrdersRepository) {
    fun invoke(csvOrderItemMetadatas: List<CsvOrderItemMetadata>) {
        val orders = CsvOrderConverter.toOrders(csvOrderItemMetadatas)

        orders.map { ordersRepository.add(it) }
    }
}