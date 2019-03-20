package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.*
import java.math.BigDecimal

class FakeSlackPoster : SlackPoster {
    override fun chatPostMessage(message: Message): PosterResponse {
        lastMessage = message
        return PostSuccess(timestamp = BigDecimal(System.currentTimeMillis() / 1000))
    }

    var lastMessage: Message? = null
}
