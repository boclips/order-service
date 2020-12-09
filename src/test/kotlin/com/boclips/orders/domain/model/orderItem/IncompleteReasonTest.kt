package com.boclips.orders.domain.model.orderItem

import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.IncompleteReason.CAPTIONS_UNAVAILABLE
import com.boclips.orders.domain.model.orderItem.IncompleteReason.LICENSE_UNAVAILABLE
import com.boclips.orders.domain.model.orderItem.IncompleteReason.PRICE_UNAVAILABLE
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import testsupport.OrderFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.temporal.ChronoUnit
import java.util.Currency
import java.util.stream.Stream

class IncompleteReasonTest {

    @ParameterizedTest
    @MethodSource("providePriceUnavailableArgs")
    fun `order item is incomplete when price is not valid`(itemPrice: Price, expected: Boolean) {
        val orderWithNoLicenseDuration = OrderFactory.orderItem(price = itemPrice)
        val result = PRICE_UNAVAILABLE.check(orderWithNoLicenseDuration)

        Assertions.assertThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("provideLicenseUnavailableArgs")
    fun `order item is incomplete when license is not valid`(itemLicense: OrderItemLicense?, expected: Boolean) {
        val orderWithNoLicenseDuration = OrderFactory.orderItem(license = itemLicense)
        val result = LICENSE_UNAVAILABLE.check(orderWithNoLicenseDuration)

        Assertions.assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `order item is incomplete when missing caption`() {
        val orderWithRequestedButUnavailableCaptions = OrderFactory.orderItem(
            transcriptRequested = true,
            video = TestFactories.video(
                captionStatus = AssetStatus.UNAVAILABLE
            )
        )
        val result = CAPTIONS_UNAVAILABLE.check(orderWithRequestedButUnavailableCaptions)

        Assertions.assertThat(result).isTrue()
    }

    companion object {

        @JvmStatic
        private fun providePriceUnavailableArgs(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    Price(amount = null, currency = Currency.getInstance("USD")),
                    true
                ),
                Arguments.of(
                    Price(amount = BigDecimal.TEN, currency = null),
                    true
                )
            )
        }

        @JvmStatic
        private fun provideLicenseUnavailableArgs(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    null,
                    true
                ),
                Arguments.of(
                    OrderItemLicense(duration = Duration.Time(amount = 10, unit = ChronoUnit.YEARS), territory = null),
                    true
                ),
                Arguments.of(
                    OrderItemLicense(duration = Duration.Time(amount = 10, unit = ChronoUnit.YEARS), territory = null),
                    true
                )
            )
        }
    }
}
