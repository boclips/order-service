package com.boclips.orders.presentation.resources

import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.presentation.orders.OrderItemResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.OrderFactory
import java.time.temporal.ChronoUnit

class OrderItemResourceTest {

    @Test
    fun `converts item with no trimming`() {
        val orderItem = OrderFactory.orderItem(
            trim = TrimRequest.NoTrimming
        )

        val orderItemResource = OrderItemResource.fromOrderItem(orderItem)

        assertThat(orderItemResource.trim).isNull()
    }

    @Test
    fun `converts item with trimming`() {
        val orderItem = OrderFactory.orderItem(
            trim = TrimRequest.WithTrimming("hello")
        )

        val orderItemResource = OrderItemResource.fromOrderItem(orderItem)

        assertThat(orderItemResource.trim).isEqualTo("hello")
    }

    @Nested
    inner class License {
        @Test
        fun `converts a valid license`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderFactory.orderItemLicense(
                    duration = Duration.Time(
                        amount = 3,
                        unit = ChronoUnit.YEARS
                    ),
                    territory = OrderItemLicense.SINGLE_REGION
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)

            assertThat(resource.licenseDuration).isEqualTo("3 Years")
            assertThat(resource.licenseTerritory).isEqualTo("Single Region")
        }

        @Test
        fun `converts a Multi Region license`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderFactory.orderItemLicense(
                    territory = OrderItemLicense.MULTI_REGION
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)

            assertThat(resource.licenseTerritory).isEqualTo("Multi Region")
        }

        @Test
        fun `converts a Worldwide license`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderFactory.orderItemLicense(
                    territory = OrderItemLicense.WORLDWIDE
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)
            assertThat(resource.licenseTerritory).isEqualTo("Worldwide")
        }

        @Test
        fun `converts a license with single duration`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderFactory.orderItemLicense(
                    duration = Duration.Time(1, ChronoUnit.YEARS)
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)
            assertThat(resource.licenseDuration).isEqualTo("1 Year")
        }

        @Test
        fun `converts a license with a description`() {
            val orderItem = OrderFactory.orderItem(
                license = OrderFactory.orderItemLicense(
                    duration = Duration.Description("Life of work")
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)
            assertThat(resource.licenseDuration).isEqualTo("Life of work")
        }
    }
}

