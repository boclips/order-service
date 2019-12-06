package com.boclips.orders.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.eventbus.events.order.OrderItem
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.Currency

class EventConverterTest {

    @Test
    fun `convert order`() {
        val order = OrderFactory.order(
            id = OrderId("the-id"),
            createdAt = ZonedDateTime.parse("2018-10-05T12:13:14Z").toInstant(),
            updatedAt = ZonedDateTime.parse("2019-10-05T12:13:14Z").toInstant(),
            orderOrganisation = OrderOrganisation(name = "Pearson"),
            fxRateToGbp = BigDecimal("2"),
            items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(videoServiceId = "the-video-id"),
                    price = PriceFactory.tenDollars()
                )
            )
        )

        val eventOrder = EventConverter().convertOrder(order)

        assertThat(eventOrder.id).isEqualTo("the-id")
        assertThat(eventOrder.createdAt).isEqualTo("2018-10-05T12:13:14Z")
        assertThat(eventOrder.updatedAt).isEqualTo("2019-10-05T12:13:14Z")
        assertThat(eventOrder.items).contains(
            OrderItem.builder()
                .priceGbp(BigDecimal("20.00"))
                .videoId(VideoId("the-video-id"))
                .build()
        )
        assertThat(eventOrder.customerOrganisationName).isEqualTo("Pearson")
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

        val eventOrder = EventConverter().convertOrder(order)

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

        val eventOrder = EventConverter().convertOrder(order)

        assertThat(eventOrder.items.first().priceGbp).isEqualTo("0.00")
    }
}