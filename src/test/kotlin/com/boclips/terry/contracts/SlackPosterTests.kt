package com.boclips.terry.contracts

import com.boclips.terry.FakeSlackPoster
import com.boclips.terry.infrastructure.outgoing.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class FakeSlackPosterTests : SlackPosterTests() {
    @Before
    fun setUp() {
        poster = FakeSlackPoster()
    }
}

class HTTPSlackPosterTests : SlackPosterTests() {
    @Before
    fun setUp() {
        poster = HTTPSlackPoster(
                slackURI = "https://slack.com/api/chat.postMessage",
                botToken = System.getenv("SLACK_BOT_TOKEN")
        )
    }
}

@org.junit.Ignore
abstract class SlackPosterTests {
    var poster: SlackPoster? = null
    private val timeout = BigDecimal(30)

    @Test
    fun `successfully posts to a channel`() {
        val begin = BigDecimal(System.currentTimeMillis() / 1000)
        val response = poster!!.chatPostMessage(Message(
                text = "Hi there",
                channel = "#terry-test-output"
        ))
        when (response) {
            is PostSuccess ->
                assertThat(response.timestamp)
                        .isGreaterThanOrEqualTo(begin)
                        .isLessThan(begin + timeout)
            is PostFailure ->
                fail<String>("Post failed: $response")
        }
    }
}

