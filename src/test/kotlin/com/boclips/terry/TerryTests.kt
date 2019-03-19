package com.boclips.terry

import com.boclips.terry.application.Terry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TerryTests {
    @Test
    fun `provides information about his capabilities`() {
        val terry = Terry()
        assertThat(terry.help()).isEqualTo("Sorry m8, I'm being built rn")
    }
}
