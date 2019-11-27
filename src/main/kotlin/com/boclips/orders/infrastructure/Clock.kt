package com.boclips.orders.infrastructure

interface Clock {
    fun read(): Long
}
