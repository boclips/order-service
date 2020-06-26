package com.boclips.orders.application.orders.converters.legacy

import com.boclips.orders.domain.model.OrderStatus

object OrderStatusConverter {
    fun from(status: String): OrderStatus {
        return when (status) {
            "COMPLETED" ->
                OrderStatus.READY
            "CONFIRMED" ->
                OrderStatus.INCOMPLETED
            "OPEN" ->
                OrderStatus.INCOMPLETED
            "PROCESSING" ->
                OrderStatus.INCOMPLETED
            "CANCELLED" ->
                OrderStatus.CANCELLED
            else ->
                OrderStatus.INVALID
        }
    }
}
