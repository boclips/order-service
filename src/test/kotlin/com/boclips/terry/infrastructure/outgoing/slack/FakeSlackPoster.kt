package com.boclips.terry.infrastructure.outgoing.slack

import com.boclips.terry.Fake

class FakeSlackPoster : Fake, SlackPoster {
    lateinit var slackMessages: List<SlackMessage>
    var nextResponse: PosterResponse? = null

    init {
        reset()
    }

    override fun reset() {
        slackMessages = emptyList()
    }

    fun respondWith(response: PosterResponse): FakeSlackPoster {
        nextResponse = response
        return this
    }

    override fun chatPostMessage(slackMessage: SlackMessage): PosterResponse =
            nextResponse!!.also { slackMessages = listOf(slackMessage) }
}
