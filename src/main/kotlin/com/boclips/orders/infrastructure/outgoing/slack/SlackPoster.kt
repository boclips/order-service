package com.boclips.orders.infrastructure.outgoing.slack

interface SlackPoster {
    fun chatPostMessage(
        slackMessage: SlackMessage,
        url: String
    ): PosterResponse
}
