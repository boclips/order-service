package com.boclips.orders.presentation

import com.boclips.orders.application.orders.BroadcastOrders
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/orders/actions")
class AdminController(
    private val broadcastOrders: BroadcastOrders
) {
    @PostMapping("/broadcast_orders")
    fun issueBroadcastOrders() =
        broadcastOrders()
}