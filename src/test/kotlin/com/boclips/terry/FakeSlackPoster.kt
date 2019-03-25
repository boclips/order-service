package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.slack.PosterResponse
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster

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
