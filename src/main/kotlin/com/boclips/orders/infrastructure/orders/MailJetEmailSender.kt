package com.boclips.orders.infrastructure.orders

import com.boclips.orders.config.properties.MailJetProperties
import com.boclips.orders.config.properties.OrderConfirmedEmailProperties
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.service.EmailSender
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import mu.KLogging
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class MailJetEmailSender(
    private val client: MailjetClient,
    private val orderConfirmedEmailProperties: OrderConfirmedEmailProperties,
    private val mailJetProperties: MailJetProperties
) : EmailSender {
    companion object : KLogging()

    override fun sendOrderConfirmation(order: Order) {
        logger.info { "Trying to send order confirmation for order ID: ${order.id.value}" }
        val result = client.post(buildOrderConfirmationEmail(order))

        if (result?.status == 200) {
            logger.info { "Successfully sent email confirmation for ${order.id.value}" }
        } else {
            logger.warn { "Sending email confirmation error: ${result.rawResponseContent}" }
        }
    }

    private fun buildOrderConfirmationEmail(order: Order): MailjetRequest {
        val receiverEmail = when (order.requestingUser) {
            is OrderUser.CompleteUser -> order.requestingUser.email
            is OrderUser.BasicUser -> throw MissingEmailException(order.requestingUser)
        }

        val orderId = order.id.value

        return MailjetRequest(Emailv31.resource)
            .property(
                Emailv31.MESSAGES,
                JSONArray()
                    .put(
                        JSONObject()
                            .put(
                                Emailv31.Message.FROM, JSONObject()
                                    .put("Email", "delivery@boclips.com")
                                    .put("Name", "The Boclips team")
                            )
                            .put(
                                Emailv31.Message.TO, JSONArray()
                                    .put(
                                        JSONObject()
                                            .put("Email", receiverEmail)
                                    )
                            )
                            .put(Emailv31.Message.TEMPLATEID, mailJetProperties.templateId.toInt())
                            .put(Emailv31.Message.TEMPLATELANGUAGE, true)
                            .put(Emailv31.Message.SUBJECT, "Your order has been confirmed")
                            .put(
                                Emailv31.Message.VARIABLES, JSONObject()
                                    .put("orderId", orderId)
                                    .put(
                                        "orderLink", "${orderConfirmedEmailProperties.baseUrl}/$orderId"
                                    )
                            )
                    )
            )
    }
}
