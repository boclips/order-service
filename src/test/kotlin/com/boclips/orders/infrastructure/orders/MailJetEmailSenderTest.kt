package com.boclips.orders.infrastructure.orders

import com.boclips.orders.config.properties.MailJetProperties
import com.boclips.orders.config.properties.OrderConfirmedEmailProperties
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUser
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Emailv31
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.OrderFactory

class MailJetEmailSenderTest {

    val mailjetClient = mock<MailjetClient>()

    lateinit var mailJetEmailSender: MailJetEmailSender

    @BeforeEach
    fun before() {
        val orderConfirmedEmailProperties = OrderConfirmedEmailProperties(baseUrl = "https://boclips.com")
        mailJetEmailSender = MailJetEmailSender(
            client = mailjetClient,
            orderConfirmedEmailProperties = orderConfirmedEmailProperties,
            mailJetProperties = MailJetProperties(
                apiKey = "ignored",
                apiSecretKey = "ignored",
                templateId = "101"
            )
        )
    }

    @Test
    fun `should send order confirmation email`() {
        val order = OrderFactory.order(
            id = OrderId("123"),
            requestingUser = OrderUser.CompleteUser(
                firstName = "M",
                lastName = "J",
                email = "dont-really-care@gg.com"
            )
        )

        mailJetEmailSender.sendOrderConfirmation(order)

        val captor = argumentCaptor<MailjetRequest>()
        verify(mailjetClient, times(1)).post(captor.capture())

        val message = captor.firstValue.bodyJSON.getJSONArray(Emailv31.MESSAGES).getJSONObject(0)
        val variables = message.getJSONObject(Emailv31.Message.VARIABLES)

        Assertions.assertThat(message.getJSONArray(Emailv31.Message.TO).getJSONObject(0).getString("Email"))
            .isEqualTo("dont-really-care@gg.com")
        Assertions.assertThat(message.getNumber(Emailv31.Message.TEMPLATEID))
            .isEqualTo(101)

        Assertions.assertThat(variables.getString("orderId")).isEqualTo("123")
        Assertions.assertThat(variables.getString("orderLink")).isEqualTo("https://boclips.com/123")
    }

    @Test
    fun `throws when trying to email a basic user`() {
        val order = OrderFactory.order(
            id = OrderId("123"),
            requestingUser = OrderUser.BasicUser(
                label = "to the moon"
            )
        )

        assertThrows<MissingEmailException> {
            mailJetEmailSender.sendOrderConfirmation(order)
        }
    }
}
