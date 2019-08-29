package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import testsupport.TestFactories

class OrderItemResourceTest {

    @Test
    fun `converts item with no trimming`() {
        val orderItem = TestFactories.orderItem(
            trim = TrimRequest.NoTrimming
        )

        val orderItemResource = OrderItemResource.fromOrderItem(orderItem)

        assertThat(orderItemResource.trim).isNull()
    }

    @Test
    fun `converts item with trimming`() {
        val orderItem = TestFactories.orderItem(
            trim = TrimRequest.WithTrimming("hello")
        )

        val orderItemResource = OrderItemResource.fromOrderItem(orderItem)

        assertThat(orderItemResource.trim).isEqualTo("hello")
    }
}

