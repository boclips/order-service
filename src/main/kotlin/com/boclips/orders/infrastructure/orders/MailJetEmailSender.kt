package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.service.EmailSender
import com.mailjet.client.ClientOptions
import com.mailjet.client.MailjetClient
import com.mailjet.client.transactional.SendContact
import com.mailjet.client.transactional.SendEmailsRequest
import com.mailjet.client.transactional.TrackOpens
import com.mailjet.client.transactional.TransactionalEmail
import com.mailjet.client.transactional.response.SendEmailsResponse
import mu.KLogging
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class MailJetEmailSender : EmailSender {
    companion object : KLogging()

    @PostConstruct
    override fun sendOrderConfirmation() {
        val options = ClientOptions.builder()
            .apiKey("96b0ee69361f961824b9f16589f35e16")
            .apiSecretKey("c61c1d5991a67ded5e40c4fb4f3ac569")
            .build()

        val client = MailjetClient(options)

        val message1: TransactionalEmail = TransactionalEmail
            .builder()
            .to(SendContact("matt@boclips.com", "Matt Jones"))
            .from(SendContact("delivery@boclips.com", "The boclips team"))
            .htmlPart("<h1>This is the HTML content of the mail</h1>")
            .subject("Your order is on it's way to the mooooooon 🚀 🚀")
            .trackOpens(TrackOpens.ENABLED)
            .header("test-header-key", "test-value")
            .customID("custom-id-value")
            .build()

        val request: SendEmailsRequest = SendEmailsRequest
            .builder()
            .message(message1) // you can add up to 50 messages per request
            .build()

        val response: SendEmailsResponse = request.sendWith(client)
    }
}
