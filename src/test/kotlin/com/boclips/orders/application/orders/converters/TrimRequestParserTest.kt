package com.boclips.orders.application.orders.converters

import com.boclips.orders.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrimRequestParserTest {
    @Test
    fun `converting null`() {
        val trimRequest = null.parseTrimRequest()
        assertThat(trimRequest).isEqualTo(TrimRequest.NoTrimming)
    }

    @Test
    fun `converting empty string`() {
        val trimRequest = "".parseTrimRequest()
        assertThat(trimRequest).isEqualTo(TrimRequest.NoTrimming)
    }

    @Test
    fun `converting a valid trim request`() {
        val trimRequest = "10 - 100".parseTrimRequest()
        assertTrue(trimRequest is TrimRequest.WithTrimming)
        assertThat((trimRequest as TrimRequest.WithTrimming).label).isEqualTo("10 - 100")
    }
}
