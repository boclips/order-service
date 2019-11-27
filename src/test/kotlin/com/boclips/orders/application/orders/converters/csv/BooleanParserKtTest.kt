package com.boclips.orders.application.orders.converters.csv

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BooleanParserKtTest {

    @Test
    fun `converts transcript requests to true ignoring case`() {
        assertThat("YES".parseBoolean()).isEqualTo(true)
    }

    @Test
    fun `converts transcript requests to true`() {
        assertThat("yes".parseBoolean()).isEqualTo(true)
    }

    @Test
    fun `converts transcript requests to false`() {
        assertThat("No".parseBoolean()).isEqualTo(false)
    }

    @Test
    fun `converts transcript requests to false when empty`() {
        assertThat("".parseBoolean()).isEqualTo(false)
    }

    @Test
    fun `converts transcript requests to false when null`() {
        assertThat(null.parseBoolean()).isEqualTo(false)
    }

}
