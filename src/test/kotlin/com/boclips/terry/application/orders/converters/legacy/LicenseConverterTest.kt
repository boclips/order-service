package com.boclips.terry.application.orders.converters.legacy

import com.boclips.terry.application.exceptions.InvalidLegacyOrderItemLicense
import com.boclips.terry.application.orders.converters.legacy.LicenseConverter
import com.boclips.terry.domain.model.orderItem.Duration
import com.boclips.terry.domain.model.orderItem.OrderItemLicense
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.TestFactories
import java.time.temporal.ChronoUnit

internal class LicenseConverterTest {

    @Test
    fun `converts a single region license`() {
        val license = TestFactories.legacyOrderItemLicense(
            code = "10YR_SR"
        )

        val convertedLicense = LicenseConverter.toOrderItemLicense(license)

        assertThat(convertedLicense).isEqualTo(
            OrderItemLicense(
                Duration.Time(amount = 10, unit = ChronoUnit.YEARS),
                territory = OrderItemLicense.SINGLE_REGION
            )
        )
    }

    @Test
    fun `converts a multi region license`() {
        val license = TestFactories.legacyOrderItemLicense(
            code = "5YR_MR"
        )

        val convertedLicense = LicenseConverter.toOrderItemLicense(license)

        assertThat(convertedLicense).isEqualTo(
            OrderItemLicense(
                Duration.Time(amount = 5, unit = ChronoUnit.YEARS),
                territory = OrderItemLicense.MULTI_REGION
            )
        )
    }

    @Test
    fun `converts a worldwide license`() {
        val license = TestFactories.legacyOrderItemLicense(
            code = "3YR_WW"
        )

        val convertedLicense = LicenseConverter.toOrderItemLicense(license)

        assertThat(convertedLicense).isEqualTo(
            OrderItemLicense(
                Duration.Time(amount = 3, unit = ChronoUnit.YEARS),
                territory = OrderItemLicense.WORLDWIDE
            )
        )
    }

    @Test
    fun `throws for invalid territory`() {
        assertThrows<InvalidLegacyOrderItemLicense> {
            val license = TestFactories.legacyOrderItemLicense(
                code = "3YR_XYZ"
            )

            LicenseConverter.toOrderItemLicense(license)
        }
    }

    @Test
    fun `throws for invalid license`() {
        assertThrows<InvalidLegacyOrderItemLicense> {
            val license = TestFactories.legacyOrderItemLicense(
                code = "boohoo"
            )

            LicenseConverter.toOrderItemLicense(license)
        }
    }
}
