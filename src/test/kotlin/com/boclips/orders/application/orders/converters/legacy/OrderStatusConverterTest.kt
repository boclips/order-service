package com.boclips.orders.application.orders.converters.legacy

import com.boclips.orders.domain.model.OrderStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OrderStatusConverterTest {
    @Test
    fun `converts valid strings to domain object`() {
        assertThat(OrderStatusConverter.from("COMPLETED")).isEqualTo(OrderStatus.READY)
        assertThat(OrderStatusConverter.from("CONFIRMED")).isEqualTo(OrderStatus.INCOMPLETED)
        assertThat(OrderStatusConverter.from("OPEN")).isEqualTo(OrderStatus.INCOMPLETED)
        assertThat(OrderStatusConverter.from("PROCESSING")).isEqualTo(OrderStatus.INCOMPLETED)
        assertThat(OrderStatusConverter.from("CANCELLED")).isEqualTo(OrderStatus.CANCELLED)
    }

    @Test
    fun `defaults to invalid`() {
        assertThat(OrderStatusConverter.from("invalid")).isEqualTo(OrderStatus.INVALID)
    }
}

