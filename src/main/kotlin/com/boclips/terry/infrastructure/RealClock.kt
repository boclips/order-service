package com.boclips.terry.infrastructure

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component

@Component
class RealClock : Clock {
    override fun read(): Long = System.currentTimeMillis() / 1000
}
