package com.boclips.terry.application.orders.converters.csv

import com.boclips.terry.domain.model.orderItem.Duration
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.temporal.ChronoUnit

class LicenseDurationParserKtTest {
    @Test
    fun `converts license duration when a number`() {
        val duration = "5".parseLicenseDuration()

        Assertions.assertThat(duration).isEqualTo(Duration.Time(5, ChronoUnit.YEARS))
    }

    @Test
    fun `converts license duration when a string`() {
        val duration = "Life of Work".parseLicenseDuration()

        Assertions.assertThat(duration).isEqualTo(Duration.Description("Life of Work"))
    }

    @Test
    fun `returns null when license is empty`() {
        val duration = "".parseLicenseDuration()

        Assertions.assertThat(duration).isNull()
    }

    @Test
    fun `returns null when license is null`() {
        val duration = null.parseLicenseDuration()

        Assertions.assertThat(duration).isNull()
    }
}
