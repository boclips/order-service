package com.boclips.terry.application

import com.boclips.terry.domain.model.orderItem.TrimRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrimmingConverterTest {
    @Test
    fun `converting null`() {
        val trimRequest = TrimmingConverter.toTrimRequest(null)
        assertThat(trimRequest).isEqualTo(TrimRequest.NoTrimming)
    }

    @Test
    fun `converting empty string`() {
        val trimRequest = TrimmingConverter.toTrimRequest("")
        assertThat(trimRequest).isEqualTo(TrimRequest.NoTrimming)
    }

    @Test
    fun `converting a valid trim request`() {
        val trimRequest = TrimmingConverter.toTrimRequest("10 - 100")
        assertTrue(trimRequest is TrimRequest.WithTrimming)
        assertThat((trimRequest as TrimRequest.WithTrimming).label).isEqualTo("10 - 100")
    }
}
