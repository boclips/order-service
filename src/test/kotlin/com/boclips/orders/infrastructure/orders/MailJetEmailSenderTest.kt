package com.boclips.orders.infrastructure.orders

import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderUser
import com.mailjet.client.MailjetClient
import com.mailjet.client.MailjetRequest
import com.mailjet.client.resource.Email
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
        mailJetEmailSender = MailJetEmailSender(mailjetClient)
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
        Assertions.assertThat(captor.firstValue.bodyJSON.getString(Email.TO)).isEqualTo("dont-really-care@gg.com")
        Assertions.assertThat(captor.firstValue.bodyJSON.getString(Email.TEXTPART)).contains("123")
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
