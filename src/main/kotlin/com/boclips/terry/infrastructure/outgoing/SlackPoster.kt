package com.boclips.terry.infrastructure.outgoing

interface SlackPoster {
    fun chatPostMessage(channel: String, text: String): PosterResponse
}
