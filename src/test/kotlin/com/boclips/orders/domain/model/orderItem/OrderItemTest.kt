package com.boclips.orders.domain.model.orderItem

import com.boclips.orders.domain.model.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories

class OrderItemTest {
    @Test
    fun `order item is ready when all pricing information and both videos and captions are available`() {
        val orderItem = OrderFactory.orderItem(
            price = PriceFactory.onePound(),
            video = TestFactories.video(
                downloadableVideoStatus = AssetStatus.AVAILABLE,
                captionStatus = AssetStatus.AVAILABLE
            )
        )

        assertThat(orderItem.status).isEqualTo(OrderItemStatus.READY)
    }

    @Nested
    inner class InProgressItems {
        @Test
        fun `order item is in progress when captions are processing`() {
            val orderItem = OrderFactory.orderItem(video = TestFactories.video(
                captionStatus = AssetStatus.PROCESSING
            ))

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.IN_PROGRESS)
        }

        @Test
        fun `order item is in progress when captions are requested`() {
            val orderItem = OrderFactory.orderItem(video = TestFactories.video(
                captionStatus = AssetStatus.REQUESTED
            ))

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.IN_PROGRESS)
        }
    }


    @Nested
    inner class IncompletedItems {
        @Test
        fun `order item is incomplete when no price available`() {
            val orderItem = OrderFactory.orderItem(price = Price(amount = null, currency = null))

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when downloadable video is not available`() {
            val orderItem = OrderFactory.orderItem(video = TestFactories.video(
                downloadableVideoStatus = AssetStatus.UNAVAILABLE)
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when missing license information`() {
            val orderItem = OrderFactory.orderItem(
                license = null
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when missing caption`() {
            val orderItem = OrderFactory.orderItem(video = TestFactories.video(
                captionStatus = AssetStatus.UNAVAILABLE)
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }
    }
}

