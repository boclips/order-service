package com.boclips.terry.infrastructure

class RealClock : Clock {
    override fun read(): Long = System.currentTimeMillis() / 1000
}
