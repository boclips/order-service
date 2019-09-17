package com.boclips.terry.common

object Do {
    inline infix fun <reified T> exhaustive(any: T?) = any
}
