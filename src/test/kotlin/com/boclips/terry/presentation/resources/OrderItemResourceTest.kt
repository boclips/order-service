package com.boclips.terry.presentation.resources

import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.Territory
import com.boclips.terry.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.time.temporal.ChronoUnit

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

    @Nested
    inner class License {
        @Test
        fun `converts a valid license`() {
            val orderItem = TestFactories.orderItem(
                license = TestFactories.orderItemLicense(
                    duration = Duration(
                        amount = 3,
                        unit = ChronoUnit.YEARS
                    ),
                    territory = Territory.SINGLE_REGION
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)

            assertThat(resource.licenseDuration).isEqualTo("3 Years")
            assertThat(resource.licenseTerritory).isEqualTo("Single Region")
        }

        @Test
        fun `converts a Multi Region license`() {
            val orderItem = TestFactories.orderItem(
                license = TestFactories.orderItemLicense(
                    territory = Territory.MULTI_REGION
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)

            assertThat(resource.licenseTerritory).isEqualTo("Multi Region")
        }

        @Test
        fun `converts a Worldwide license`() {
            val orderItem = TestFactories.orderItem(
                license = TestFactories.orderItemLicense(
                    territory = Territory.WORLDWIDE
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)
            assertThat(resource.licenseTerritory).isEqualTo("Worldwide")
        }

        @Test
        fun `converts a license with single duration`() {
            val orderItem = TestFactories.orderItem(
                license = TestFactories.orderItemLicense(
                    duration = Duration(1, ChronoUnit.YEARS)
                )
            )

            val resource = OrderItemResource.fromOrderItem(orderItem)
            assertThat(resource.licenseDuration).isEqualTo("1 Year")
        }
    }
}

