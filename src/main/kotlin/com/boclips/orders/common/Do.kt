package com.boclips.orders.common

object Do {
    inline infix fun <reified T> exhaustive(any: T?) = any
}
