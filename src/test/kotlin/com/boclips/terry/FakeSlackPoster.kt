package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.*
import java.math.BigDecimal

class FakeSlackPoster(private val response: PosterResponse = PostSuccess(timestamp = BigDecimal(System.currentTimeMillis() / 1000))) : SlackPoster {
    override fun chatPostMessage(message: Message): PosterResponse {
        lastMessage = message
        return response
    }

    var lastMessage: Message? = null
}
