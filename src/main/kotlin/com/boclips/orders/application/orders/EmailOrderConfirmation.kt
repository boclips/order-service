package com.boclips.orders.application.orders

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.service.EmailSender

class EmailOrderConfirmation(private val emailSender: EmailSender) {
    @BoclipsEventListener
    fun onOrderPlaced(event: OrderCreated) {
        val order = event.order
        // emailSender.sendOrderConfirmation(
        //     Order(
        //         id = OrderId(value = order.id),
        //         legacyOrderId = order.legacyOrderId
        //         )
        // )
    }
}
