package com.boclips.orders.domain.model.orderItem

import com.boclips.orders.domain.model.Price
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.*

class OrderItemTest {

    @Nested
    inner class ReadyItems {
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

        @Test
        fun `order item is ready when all pricing information and captions are available, video not available does not block`() {
            val orderItem = OrderFactory.orderItem(
                price = PriceFactory.onePound(),
                video = TestFactories.video(
                    downloadableVideoStatus = AssetStatus.UNAVAILABLE,
                    captionStatus = AssetStatus.AVAILABLE
                )
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.READY)
        }
    }

    @Nested
    inner class InProgressItems {
        @Test
        fun `order item is in progress when captions are processing`() {
            val orderItem = OrderFactory.orderItem(
                video = TestFactories.video(
                    captionStatus = AssetStatus.PROCESSING
                )
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.IN_PROGRESS)
        }

        @Test
        fun `order item is in progress when captions are requested`() {
            val orderItem = OrderFactory.orderItem(
                video = TestFactories.video(
                    captionStatus = AssetStatus.REQUESTED
                )
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.IN_PROGRESS)
        }
    }

    @Nested
    inner class IncompletedItems {

        @Test
        fun `order item is incomplete when no price amount available`() {
            val orderItem = OrderFactory.orderItem(price = Price(amount = null, currency = Currency.getInstance("USD")))

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when no price currency available`() {
            val orderItem = OrderFactory.orderItem(price = Price(amount = BigDecimal.TEN, currency = null))

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when downloadable video is not available`() {
            val orderItem = OrderFactory.orderItem(
                video = TestFactories.video(
                    downloadableVideoStatus = AssetStatus.UNAVAILABLE
                )
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.READY)
        }

        @Test
        fun `order item is incomplete when missing license information`() {
            val orderItem = OrderFactory.orderItem(
                license = null
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when missing license territory`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderItemLicense(duration = Duration.Time(amount = 10, unit = ChronoUnit.YEARS), territory = null)
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when missing license duration`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderItemLicense(duration = null, territory = OrderItemLicense.SINGLE_REGION)
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is incomplete when missing caption`() {
            val orderItem = OrderFactory.orderItem(
                captionsRequested = true,
                video = TestFactories.video(
                    captionStatus = AssetStatus.UNAVAILABLE
                )
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.INCOMPLETED)
        }

        @Test
        fun `order item is ready when missing caption but a user has not requested them`() {
            val orderItem = OrderFactory.orderItem(
                captionsRequested = false,
                video = TestFactories.video(
                    captionStatus = AssetStatus.UNAVAILABLE
                )
            )

            assertThat(orderItem.status).isEqualTo(OrderItemStatus.READY)
        }
    }
}
