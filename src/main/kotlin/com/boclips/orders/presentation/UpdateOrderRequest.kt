package com.boclips.orders.presentation

data class UpdateOrderRequest(
    val organisation: String? = null,
    val currency: String? = null,
    val status: UpdateOrderStatusRequest? = null
)

enum class UpdateOrderStatusRequest {
    DELIVERED,
    READY;
}
