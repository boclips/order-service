package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.infrastructure.outgoing.Message

interface SlackPoster {
    fun chatPostMessage(message: Message): PosterResponse
}
