package com.boclips.orders.domain.service

import com.boclips.orders.domain.model.Order

interface EmailSender {
    fun sendOrderConfirmation(order: Order)
}
