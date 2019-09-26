package com.boclips.terry.application.orders.converters.csv

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset

class DateParserKtTest {

    @Test
    fun `when valid date`() {
        assertThat("30/06/1988".parseCsvDate()).isEqualTo(
            LocalDate.of(1988, Month.JUNE, 30).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
    }

    @Test
    fun `when invalid date`() {
        assertThat("an invalid date".parseCsvDate()).isNull()
    }

    @Test
    fun `when blank`() {
        assertThat("".parseCsvDate()).isNull()
    }

    @Test
    fun `when null`() {
        assertThat(null.parseCsvDate()).isNull()
    }

    @Test
    fun `defaults to backup if specified`() {
        assertThat(null.parseCsvDate("30/06/1988")).isEqualTo(
            LocalDate.of(1988, Month.JUNE, 30).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
    }

    @Test
    fun `can handle two digit year`() {
        assertThat("20/01/19".parseCsvDate()).isEqualTo(
            LocalDate.of(2019, Month.JANUARY, 20).atStartOfDay().toInstant(ZoneOffset.UTC)
        )
    }
}
