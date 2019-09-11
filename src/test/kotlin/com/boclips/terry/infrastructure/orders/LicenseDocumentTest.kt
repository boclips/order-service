package com.boclips.terry.infrastructure.orders

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.temporal.ChronoUnit

class LicenseDocumentTest {
    @Test
    fun `is a valid time`() {
        val licenseDocument = LicenseDocument(
            amount = 3,
            unit = ChronoUnit.YEARS,
            description = null,
            territory = "hi"
        )

        assertThat(licenseDocument.isValidTime()).isEqualTo(true)
    }

    @Test
    fun `is a valid description`() {
        val licenseDocument = LicenseDocument(
            amount = null,
            unit = null,
            description = "some description",
            territory = "hi"
        )

        assertThat(licenseDocument.isValidDescription()).isEqualTo(true)
    }
}
