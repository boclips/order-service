package com.boclips.terry

import com.boclips.terry.infrastructure.outgoing.Message
import com.boclips.terry.infrastructure.outgoing.slack.PosterResponse
import com.boclips.terry.infrastructure.outgoing.slack.SlackPoster

class FakeSlackPoster : Fake, SlackPoster {
    lateinit var messages: List<Message>
    var nextResponse: PosterResponse? = null

    init {
        reset()
    }

    override fun reset() {
        messages = emptyList()
    }

    fun respondWith(response: PosterResponse): FakeSlackPoster {
        nextResponse = response
        return this
    }

    override fun chatPostMessage(message: Message): PosterResponse =
            nextResponse!!.also { messages = listOf(message) }
}
