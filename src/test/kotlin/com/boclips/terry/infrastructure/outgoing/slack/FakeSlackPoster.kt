package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.Fake

class FakeSlackPoster : Fake, SlackPoster {
    lateinit var slackMessages: List<SlackMessage>
    var nextResponse: PosterResponse? = null

    init {
        reset()
    }

    override fun reset(): Fake = this
        .also { slackMessages = emptyList() }

    fun respondWith(response: PosterResponse): FakeSlackPoster = this
        .also { nextResponse = response }

    override fun chatPostMessage(slackMessage: SlackMessage): PosterResponse = nextResponse!!
        .also { slackMessages = listOf(slackMessage) }
}
