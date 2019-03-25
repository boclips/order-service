package com.boclips.terry.infrastructure.outgoing.slack

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class FakeSlackPosterTests : SlackPosterTests() {
    @Before
    fun setUp() {
        poster = FakeSlackPoster().respondWith(PostSuccess(timestamp = BigDecimal(System.currentTimeMillis() / 1000 + 1)))
        failingPoster = FakeSlackPoster().respondWith(PostFailure(message = "401 UNAUTHORIZED"))
    }
}

class HTTPSlackPosterTests : SlackPosterTests() {
    @Before
    fun setUp() {
        poster = HTTPSlackPoster(
                slackURI = "https://slack.com/api/chat.postMessage",
                botToken = System.getenv("SLACK_BOT_TOKEN")
        )
        failingPoster = HTTPSlackPoster(
                slackURI = "https://httpbin.org/status/401",
                botToken = "bad-token"
        )
    }
}

@org.junit.Ignore
abstract class SlackPosterTests {
    var poster: SlackPoster? = null
    var failingPoster: SlackPoster? = null
    private val timeout = BigDecimal(30)

    @Test
    fun `successfully posts to a channel`() {
        val begin = BigDecimal(System.currentTimeMillis() / 1000)
        val response = poster!!.chatPostMessage(SlackMessage(
                text = "Hi there",
                channel = "#terry-test-output",
                attachments = listOf(Attachment(
                        imageUrl = "https://www.boclips.com/hubfs/Boclips_November2018%20Theme/image/terry-610548e89d54257dccc9174c262f53e7.png",
                        title = "This is a really cool video",
                        videoId = "A Video Id",
                        type = "YouTube",
                        playbackId = "12345561359"
                ))
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

    @Test
    fun `failures produce PostFailures`() {
        when (val response = failingPoster!!.chatPostMessage(SlackMessage(
                text = "I hope this won't work",
                channel = "#terry-test-output"
        ))) {
            is PostSuccess ->
                fail<String>("Expected post to Slack to fail, but it was successful: $response")
            is PostFailure ->
                assertThat(response.message).isEqualTo("401 UNAUTHORIZED")
        }
    }
}
