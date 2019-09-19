package com.boclips.terry.application.orders.converters

import com.boclips.terry.domain.model.OrderStatus

object OrderStatusConverter {
    fun from(status: String): OrderStatus {
        return when (status) {
            "COMPLETED" ->
                OrderStatus.COMPLETED
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
