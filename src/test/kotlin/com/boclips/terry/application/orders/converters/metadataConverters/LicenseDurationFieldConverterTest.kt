package com.boclips.terry.application.orders.converters.metadataConverters

import com.boclips.terry.application.orders.exceptions.InvalidLicenseCsvException
import com.boclips.terry.domain.model.orderItem.Duration
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

import testsupport.TestFactories
import java.time.temporal.ChronoUnit

class LicenseDurationFieldConverterTest {

    @Test
    fun `converts license duration when a number`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(licenseDuration = "5")

        val duration = LicenseDurationFieldConverter.convert(csvMetadataItem)

        Assertions.assertThat(duration).isEqualTo(Duration.Time(5, ChronoUnit.YEARS))
    }

    @Test
    fun `converts license duration when a string`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(licenseDuration = "Life of Work")

        val duration = LicenseDurationFieldConverter.convert(csvMetadataItem)

        Assertions.assertThat(duration).isEqualTo(Duration.Description("Life of Work"))
    }

    @Test
    fun `throws when license is empty`() {
        val csvMetadataItem = TestFactories.csvOrderItemMetadata(licenseDuration = "")

        org.junit.jupiter.api.assertThrows<InvalidLicenseCsvException> {
            LicenseDurationFieldConverter.convert(csvMetadataItem)
        }

    }
}
