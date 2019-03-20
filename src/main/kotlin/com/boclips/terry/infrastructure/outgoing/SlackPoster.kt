package com.boclips.terry.infrastructure.outgoing

interface SlackPoster {
    fun chatPostMessage(message: Message): PosterResponse
}
