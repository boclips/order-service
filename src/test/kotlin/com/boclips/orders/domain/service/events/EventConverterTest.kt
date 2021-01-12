package com.boclips.orders.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.eventbus.events.order.OrderItem
import com.boclips.eventbus.events.order.OrderUser
import com.boclips.orders.domain.model.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.Currency
import com.boclips.eventbus.events.order.OrderStatus as EventOrderStatus

class EventConverterTest {

    val eventConverter = EventConverter()

    @Test
    fun `convert order`() {
        val order = OrderFactory.order(
            id = OrderId("the-id"),
            legacyOrderId = "identitatem",
            createdAt = ZonedDateTime.parse("2018-10-05T12:13:14Z").toInstant(),
            updatedAt = ZonedDateTime.parse("2019-10-05T12:13:14Z").toInstant(),
            orderOrganisation = OrderOrganisation(name = "Pearson"),
            fxRateToGbp = BigDecimal("2"),
            items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(videoServiceId = "the-video-id"),
                    price = PriceFactory.tenDollars()
                )
            ),
            status = OrderStatus.READY,
            authorisingUser = OrderFactory.completeOrderUser(
                firstName = "Pear",
                lastName = "Son",
                email = "pear@son.com",
                sourceUserId = "pson-1"
            ),
            requestingUser = OrderFactory.completeOrderUser(
                firstName = "Apple",
                lastName = "Son",
                email = "apple@son.com",
                sourceUserId = "pson-2"
            ),
            isbnOrProductNumber = "ISBN-1",
            isThroughPlatform = true,
            orderSource = OrderSource.LEGACY,
            currency = Currency.getInstance("USD")
        )

        val eventOrder = eventConverter.convertOrder(order)

        val assertUserHasFields = { user: OrderUser,
            firstName: String,
            lastName: String,
            email: String,
            userId: String ->
            assertThat(user.firstName).isEqualTo(firstName)
            assertThat(user.lastName).isEqualTo(lastName)
            assertThat(user.email).isEqualTo(email)
            assertThat(user.legacyUserId).isEqualTo(userId)
        }

        assertThat(eventOrder.id).isEqualTo("the-id")
        assertThat(eventOrder.legacyOrderId).isEqualTo("identitatem")
        assertThat(eventOrder.status).isEqualTo(EventOrderStatus.READY)
        assertThat(eventOrder.createdAt).isEqualTo("2018-10-05T12:13:14Z")
        assertThat(eventOrder.updatedAt).isEqualTo("2019-10-05T12:13:14Z")
        assertThat(eventOrder.items).contains(
            OrderItem.builder()
                .priceGbp(BigDecimal("20.00"))
                .videoId(VideoId("the-video-id"))
                .build()
        )
        assertThat(eventOrder.customerOrganisationName).isEqualTo("Pearson")
        assertNotNull(eventOrder.authorisingUser)
        assertUserHasFields(eventOrder.authorisingUser, "Pear", "Son", "pear@son.com", "pson-1")
        assertUserHasFields(eventOrder.requestingUser, "Apple", "Son", "apple@son.com", "pson-2")
        assertThat(eventOrder.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(eventOrder.fxRateToGbp).isEqualTo(BigDecimal("2"))
        assertThat(eventOrder.isbnOrProductNumber).isEqualTo("ISBN-1")
        assertThat(eventOrder.orderSource).isEqualTo(com.boclips.eventbus.events.order.OrderSource.LEGACY)
    }

    @Test
    fun `convert order status`() {
        assertThat(eventConverter.convertOrderStatus(OrderStatus.READY)).isEqualTo(EventOrderStatus.READY)
        assertThat(eventConverter.convertOrderStatus(OrderStatus.CANCELLED)).isEqualTo(EventOrderStatus.CANCELLED)
        assertThat(eventConverter.convertOrderStatus(OrderStatus.INCOMPLETED)).isEqualTo(EventOrderStatus.INCOMPLETED)
        assertThat(eventConverter.convertOrderStatus(OrderStatus.INVALID)).isEqualTo(EventOrderStatus.INVALID)
    }

    @Test
    fun `price is zero when order has no currency`() {
        val order = OrderFactory.order(
            currency = null, items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(),
                    price = Price(amount = BigDecimal.TEN, currency = null)
                )
            )
        )

        val eventOrder = eventConverter.convertOrder(order)

        assertThat(eventOrder.items.first().priceGbp).isEqualTo("0.00")
    }

    @Test
    fun `price is zero when order has no price amount`() {
        val order = OrderFactory.order(
            currency = Currency.getInstance("USD"), items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(),
                    price = Price(amount = null, currency = Currency.getInstance("USD"))
                )
            )
        )

        val eventOrder = eventConverter.convertOrder(order)

        assertThat(eventOrder.items.first().priceGbp).isEqualTo("0.00")
    }
}
