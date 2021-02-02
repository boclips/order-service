package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.service.EmailSender
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Email
import mu.KLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class MailJetEmailSender(
    private val client: MailjetClient
) : EmailSender {
    companion object : KLogging()

    override fun sendOrderConfirmation(order: Order) {
        logger.info { "trying to send order confirmation for order ID: ${order.id.value}" }
        client.post(buildOrderConfirmationEmail(order))
    }

    private fun buildOrderConfirmationEmail(order: Order): MailjetRequest {
        val receiverEmail = when (order.requestingUser) {
            is OrderUser.CompleteUser -> order.requestingUser.email
            is OrderUser.BasicUser -> throw MissingEmailException(order.requestingUser)
        }

        return MailjetRequest(Email.resource)
            .property(Email.FROMEMAIL, "delivery@boclips.com")
            .property(Email.FROMNAME, "The Boclips team")
            .property(Email.SUBJECT, "Your order has been confirmed")
            .property(
                Email.TEXTPART,
                "Your order ID is: ${order.id.value}"
            )
            .property(Email.TO, receiverEmail)
    }
}
