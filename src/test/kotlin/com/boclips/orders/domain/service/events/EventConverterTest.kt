package com.boclips.orders.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.orders.domain.model.OrderId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import testsupport.TestFactories
import java.time.ZonedDateTime

class EventConverterTest {

    @Test
    fun `convert order`() {
        val order = OrderFactory.order(
            id = OrderId("the-id"),
            createdAt = ZonedDateTime.parse("2018-10-05T12:13:14Z").toInstant(),
            updatedAt = ZonedDateTime.parse("2019-10-05T12:13:14Z").toInstant(),
            items = listOf(
                OrderFactory.orderItem(video = TestFactories.video(videoServiceId = "the-video-id"))
            )
        )

        val eventOrder = EventConverter().convertOrder(order)

        assertThat(eventOrder.id).isEqualTo("the-id")
        assertThat(eventOrder.createdAt).isEqualTo("2018-10-05T12:13:14Z")
        assertThat(eventOrder.updatedAt).isEqualTo("2019-10-05T12:13:14Z")
        assertThat(eventOrder.videoIds).containsExactly(VideoId("the-video-id"))
    }
}