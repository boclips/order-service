package com.boclips.orders.presentation

import com.boclips.orders.presentation.orders.OrderStatusResource

data class UpdateOrderRequest(
    val organisation: String? = null,
    val currency: String? = null,
    val status: OrderStatusResource? = null
)