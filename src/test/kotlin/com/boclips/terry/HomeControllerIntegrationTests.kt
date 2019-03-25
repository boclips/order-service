package com.boclips.terry

import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.Message
import com.boclips.terry.infrastructure.outgoing.slack.PostFailure
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.videos.FoundVideo
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerIntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var slackPoster: FakeSlackPoster

    @Autowired
    lateinit var slackSignature: SlackSignature

    @Autowired
    lateinit var videoService: FakeVideoService

    @Before
    fun setUp() {
        slackPoster.reset()
        videoService.reset()
    }

    @Test
    fun `root path serves a terrific message`() {
        mockMvc.perform(
                get("/"))
                .andExpect(status().isOk)
                .andExpect(xpath("h1").string(containsString("Do as I say")))
    }

    @Test
    fun `can meet Slack's verification challenge`() {
        postFromSlack("""
            {
                "token": "sometoken",
                "challenge": "iamchallenging",
                "type": "url_verification"
            }
        """)
                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.challenge", equalTo("iamchallenging")))
    }

    @Test
    fun `failing the request signature check results in 401`() {
        val timestamp = validTimestamp()
        postFromSlack(
                body = """
                    {
                        "token": "sometoken",
                        "challenge": "iamchallenging",
                        "type": "url_verification"
                    }
                """,
                timestamp = timestamp,
                signature = slackSignature.compute(timestamp.toString(), "different-body")
        )
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `sending a timestamp older than 5 minutes results in 401`() {
        postFromSlack("""
            {
                "token": "sometoken",
                "challenge": "iamchallenging",
                "type": "url_verification"
            }
        """, staleTimestamp())
                .andExpect(status().isUnauthorized)
    }

    @Test
    fun `it's a client error to send a malformed Slack verification request`() {
        postFromSlack("""
            {
                "token": "sometoken",
                "poo": "iamchallenging",
                "type": "url_verification"
            }""".trimIndent())
                .andExpect(status().is4xxClientError)
    }

    @Test
    fun `Slack mentions receive 200s and send responses`() {
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(1231231)))

        postFromSlack("""
            {
                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                "team_id": "T061EG9R6",
                "api_app_id": "A0MDYCDME",
                "event": {
                    "funny_unknown_property": "to-test-ignoring-unknown-properties",
                    "type": "app_mention",
                    "user": "U061F7AUR",
                    "text": "What ever happened to <@U0LAN0Z89>?",
                    "ts": "1515449438.000011",
                    "channel": "C0LAN2Q65",
                    "event_ts": "1515449438000011"
                },
                "type": "event_callback",
                "event_id": "Ev0MDYGDKJ",
                "event_time": 1515449438000011,
                "authed_users": [
                    "U0LAN0Z89"
                ]
            }""".trimIndent())
                .andExpect(status().isOk)
                .andExpect(content().json("{}"))

        assertThat(slackPoster.messages)
                .isEqualTo(listOf(Message(
                        text = "<@U061F7AUR> I don't do much yet",
                        channel = "C0LAN2Q65"
                )))
    }

    @Test
    fun `Slack response failure gives 500`() {
            slackPoster.nextResponse = PostFailure(message = "could not post to slack")

            postFromSlack("""
            {
                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                "team_id": "T061EG9R6",
                "api_app_id": "A0MDYCDME",
                "event": {
                    "funny_unknown_property": "to-test-ignoring-unknown-properties",
                    "type": "app_mention",
                    "user": "U061F7AUR",
                    "text": "What ever happened to <@U0LAN0Z89>?",
                    "ts": "1515449438.000011",
                    "channel": "C0LAN2Q65",
                    "event_ts": "1515449438000011"
                },
                "type": "event_callback",
                "event_id": "Ev0MDYGDKJ",
                "event_time": 1515449438000011,
                "authed_users": [
                    "U0LAN0Z89"
                ]
            }""".trimIndent())
                    .andExpect(status().is5xxServerError)
                    .andExpect(content().json("{}"))
    }

    @Test
    fun `videos are retrieved`() {
        videoService.respondWith(FoundVideo(videoId = "resolvedId", title = "Best video"))
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(98765)))

        postFromSlack("""
            {
                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                "team_id": "T061EG9R6",
                "api_app_id": "A0MDYCDME",
                "event": {
                    "type": "app_mention",
                    "user": "U061F7AUR",
                    "text": "<@U0LAN0Z89> can I get video asdfzxcv please?",
                    "ts": "1515449438.000011",
                    "channel": "C0LAN2Q65",
                    "event_ts": "1515449438000011"
                },
                "type": "event_callback",
                "event_id": "Ev0MDYGDKJ",
                "event_time": 1515449438000011,
                "authed_users": [
                    "U0LAN0Z89"
                ]
            }""".trimIndent())
                .andExpect(status().isOk)

        assertThat(videoService.lastIdRequest).isEqualTo("asdfzxcv")
        assertThat(slackPoster.messages).isEqualTo(listOf(Message(channel = "C0LAN2Q65", text = "<@U061F7AUR> Your request for video asdfzxcv: video ID resolvedId is called \"Boclips 4evah\"")))
    }

    private fun validTimestamp() = System.currentTimeMillis() / 1000 - (5 * 60)
    private fun staleTimestamp() = validTimestamp() - 1

    private fun postFromSlack(
            body: String,
            timestamp: Long = validTimestamp(),
            signature: String = slackSignature.compute(timestamp.toString(), body)
    ): ResultActions =
            mockMvc.perform(
                    post("/slack")
                            .header("X-Slack-Request-Timestamp", timestamp)
                            .header("X-Slack-Signature", signature)
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(body)
            )
}
