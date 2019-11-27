package com.boclips.orders.application.orders.converters.csv

import com.boclips.orders.domain.model.Order

sealed class OrdersResult {
    companion object {
        fun instanceOf(orders: List<Order>, errors: List<OrderConversionError>) =
            if (errors.isEmpty()) {
                Orders(orders)
            } else {
                Errors(errors)
            }
    }
}

data class Errors(val errors: List<OrderConversionError>) : OrdersResult()
data class Orders(val orders: List<Order>) : OrdersResult()

data class OrderConversionError(
    val legacyOrderId: String?,
    val message: String
)
