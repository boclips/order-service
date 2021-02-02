package com.boclips.orders.application.orders

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderSource
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.EmailSender
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class EmailOrderConfirmation(
    private val emailSender: EmailSender,
    private val ordersRepository: OrdersRepository
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun onOrderPlaced(event: OrderCreated) {
        val order = event.order
        ordersRepository.findOne(OrderId(order.id))?.let {
            if (it.orderSource == OrderSource.BOCLIPS) {
                emailSender.sendOrderConfirmation(it)
            }
        } ?: throw OrderNotFoundException(orderId = OrderId(order.id))
    }
}
