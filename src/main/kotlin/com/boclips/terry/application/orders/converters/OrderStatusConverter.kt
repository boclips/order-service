package com.boclips.terry.application.orders.converters

import com.boclips.terry.domain.model.OrderStatus

object OrderStatusConverter {
    fun from(status: String): OrderStatus {
        return when (status) {
            "COMPLETED" ->
                OrderStatus.COMPLETED
            "CONFIRMED" ->
                OrderStatus.CONFIRMED
            "CANCELLED" ->
                OrderStatus.CANCELLED
            "OPEN" ->
                OrderStatus.OPEN
            "PROCESSING" ->
                OrderStatus.PROCESSING
            else ->
                OrderStatus.INVALID
        }
    }
}
