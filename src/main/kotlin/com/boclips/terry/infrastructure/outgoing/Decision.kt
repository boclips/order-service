package com.boclips.terry.infrastructure.outgoing

data class Decision(
        val acknowledgement: Acknowledgement,
        val log: String
)
