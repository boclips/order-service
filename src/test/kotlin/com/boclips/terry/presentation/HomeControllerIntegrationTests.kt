package com.boclips.terry.presentation

import com.boclips.kalturaclient.TestKalturaClient
import com.boclips.terry.infrastructure.incoming.SlackSignature
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessageVideo
import com.boclips.terry.infrastructure.outgoing.slack.FakeSlackPoster
import com.boclips.terry.infrastructure.outgoing.slack.PostSuccess
import com.boclips.terry.infrastructure.outgoing.slack.SlackMessage
import com.boclips.terry.infrastructure.outgoing.videos.FakeVideoService
import com.boclips.terry.infrastructure.outgoing.videos.FoundKalturaVideo
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath
import java.math.BigDecimal
import java.net.URLEncoder

@ExtendWith(SpringExtension::class)
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

    @Autowired
    lateinit var kalturaClient: TestKalturaClient

    @BeforeEach
    fun setUp() {
        slackPoster.reset()
        videoService.reset()
    }

    @Test
    fun `root path serves a terrific message`() {
        mockMvc.perform(
            get("/")
        )
            .andExpect(status().isOk)
            .andExpect(xpath("h1").string(containsString("Do as I say")))
    }

    @Test
    fun `can meet Slack's verification challenge`() {
        postFromSlack(
            """
            {
                "token": "sometoken",
                "challenge": "iamchallenging",
                "type": "url_verification"
            }
        """
        )
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
        postFromSlack(
            body = """
            {
                "token": "sometoken",
                "challenge": "iamchallenging",
                "type": "url_verification"
            }
        """,
            timestamp = staleTimestamp()
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `it's a client error to send a malformed Slack verification request`() {
        postFromSlack(
            body = """
            {
                "token": "sometoken",
                "poo": "iamchallenging",
                "type": "url_verification"
            }""".trimIndent()
        )
            .andExpect(status().is4xxClientError)
    }

    @Test
    fun `Slack mentions receive 200s and send responses`() {
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(1231231)))

        postFromSlack(
            body = """
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
            }""".trimIndent()
        )
            .andExpect(status().isOk)
            .andExpect(content().json("{}"))

        assertThat(slackPoster.slackMessages)
            .isEqualTo(
                listOf(
                    SlackMessage(
                        text = "<@U061F7AUR> I don't do much yet",
                        channel = "C0LAN2Q65"
                    )
                )
            )
    }

    @Test
    fun `videos are retrieved`() {
        videoService.respondWith(
            FoundKalturaVideo(
                videoId = "resolvedId",
                title = "Boclips 4evah",
                description = "a description",
                thumbnailUrl = "blahblah",
                playbackId = "agreatplayback",
                streamUrl = null
            )
        )
        slackPoster.respondWith(PostSuccess(timestamp = BigDecimal(98765)))

        postFromSlack(
            body = """
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
            }""".trimIndent()
        )
            .andExpect(status().isOk)

        assertThat(videoService.lastIdRequest).isEqualTo("asdfzxcv")
        assertThat(slackPoster.slackMessages).isEqualTo(
            listOf(
                SlackMessage(
                    channel = "C0LAN2Q65",
                    text = "<@U061F7AUR> Here are the video details for asdfzxcv:",
                    slackMessageVideos = listOf(
                        SlackMessageVideo(
                            imageUrl = "blahblah",
                            title = "Boclips 4evah",
                            videoId = "resolvedId",
                            type = "Kaltura",
                            playbackId = "agreatplayback"
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `transcript requests (with asterisks encoded as %2A) tag videos in Kaltura`() {
        val payload = transcriptRequestPayload("0_fgc6nmmt")
        var encodedAsterisk = "%2A"
        postFromSlack(
            body = "payload=${URLEncoder.encode(payload, "utf-8")
                .replace("video+details+for+1234%3A*","video+details+for+1234%3A$encodedAsterisk")}",
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        )
            .andExpect(status().isOk)

        assertThat(kalturaClient.getBaseEntry("0_fgc6nmmt").tags).containsExactly("caption48british")
    }

    private fun transcriptRequestPayload(entryId: String): String =
            """
    {
      "type": "block_actions",
      "team": {
        "id": "T04CX5TQC",
        "domain": "boclips"
      },
      "user": {
        "id": "UBS7V80PR",
        "username": "andrew",
        "name": "andrew",
        "team_id": "T04CX5TQC"
      },
      "api_app_id": "AH343GQTZ",
      "token": "sometoken",
      "container": {
        "type": "message",
        "message_ts": "1554309414.016200",
        "channel_id": "CH1HFTDT2",
        "is_ephemeral": false
      },
      "trigger_id": "600474721686.4439197828.7925f4d9002c4b77d9773a2ee0763ef4",
      "channel": {
        "id": "CH1HFTDT2",
        "name": "terry-test-output"
      },
      "message": {
        "type": "message",
        "subtype": "bot_message",
        "text": "This content can't be displayed.",
        "ts": "1554309414.016200",
        "username": "Terry",
        "bot_id": "BH3ADPWM8",
        "blocks": [
          {
            "type": "section",
            "block_id": "GUY",
            "text": {
              "type": "mrkdwn",
              "text": "*<@UBS7V80PR> Here are the video details for 1234:*",
              "verbatim": false
            }
          },
          {
            "type": "divider",
            "block_id": "7N86C"
          },
          {
            "type": "section",
            "block_id": "BhQ9",
            "text": {
              "type": "mrkdwn",
              "text": "Is Islamic State Planning Attacks on the West?",
              "verbatim": false
            },
            "accessory": {
              "fallback": "435x250px image",
              "image_url": "https://cdnapisec.kaltura.com/p/1776261/thumbnail/entry_id/0_fgc6nmmt/height/250/vid_slices/3/vid_slice/2",
              "image_width": 435,
              "image_height": 250,
              "image_bytes": 22949,
              "type": "image",
              "alt_text": "Is Islamic State Planning Attacks on the West?"
            }
          },
          {
            "type": "section",
            "block_id": "5xB4",
            "text": {
              "type": "mrkdwn",
              "text": "*Playback ID*\n$entryId",
              "verbatim": false
            }
          },
          {
            "type": "section",
            "block_id": "m71",
            "text": {
              "type": "mrkdwn",
              "text": "*Playback Provider*\nKaltura",
              "verbatim": false
            }
          },
          {
            "type": "section",
            "block_id": "UXvaU",
            "text": {
              "type": "mrkdwn",
              "text": "*Video ID*\n5c54a6c7d8eafeecae072ca4",
              "verbatim": false
            }
          },
          {
            "type": "section",
            "block_id": "dvNbh",
            "text": {
              "type": "mrkdwn",
              "text": "Request transcript:",
              "verbatim": false
            },
            "accessory": {
              "type": "static_select",
              "placeholder": {
                "type": "plain_text",
                "text": "Choose transcript type",
                "emoji": true
              },
              "options": [
                {
                  "text": {
                    "type": "plain_text",
                    "text": "British English",
                    "emoji": true
                  },
                  "value": "british-english"
                },
                {
                  "text": {
                    "type": "plain_text",
                    "text": "US English",
                    "emoji": true
                  },
                  "value": "us-english"
                }
              ],
              "action_id": "LGn/v"
            }
          }
        ]
      },
      "response_url": "https://hooks.slack.com/actions/T04CX5TQC/599518168997/NrTHISozF3Le5AOQKOAC3aPt",
      "actions": [
        {
          "type": "static_select",
          "action_id": "LGn/v",
          "block_id": "dvNbh",
          "selected_option": {
            "text": {
              "type": "plain_text",
              "text": "British English",
              "emoji": true
            },
            "value": "{\"code\":\"british-english\",\"entryId\":\"$entryId\"}"
          },
          "placeholder": {
            "type": "plain_text",
            "text": "Choose transcript type",
            "emoji": true
          },
          "action_ts": "1554309417.644885"
        }
      ]
    }
            """.trimIndent()

    private val timestampBufferSeconds = 10

    private fun validTimestamp() =
        System.currentTimeMillis() / 1000 - (5 * 60) + timestampBufferSeconds

    private fun staleTimestamp() =
        validTimestamp() - timestampBufferSeconds - 1

    private fun postFromSlack(
        body: String,
        contentType: MediaType = MediaType.APPLICATION_JSON_UTF8,
        timestamp: Long = validTimestamp(),
        signature: String = slackSignature.compute(timestamp = timestamp.toString(), body = body)
    ): ResultActions =
        mockMvc.perform(
            post("/slack")
                .header("X-Slack-Request-Timestamp", timestamp)
                .header("X-Slack-Signature", signature)
                .contentType(contentType)
                .content(body)
        )
}
