package com.boclips.terry

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerIntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var slackPoster: FakeSlackPoster

    @Test
    fun `root path serves a terrific message`() {
        mockMvc.perform(
                get("/"))
                .andExpect(status().isOk)
                .andExpect(xpath("h1").string(containsString("Do as I say")))
    }

    @Test
    fun `can meet Slack's verification challenge`() {
        mockMvc.perform(
                post("/slack")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("""
                            {
                                "token": "sometoken",
                                "challenge": "iamchallenging",
                                "type": "url_verification"
                            }
                        """)
        )
                .andExpect(status().isOk)
                .andExpect(header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.challenge", equalTo("iamchallenging")))
    }

    @Test
    fun `it's a client error to send a malformed Slack verification request`() {
        mockMvc.perform(
                post("/slack")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("""
                            {
                                "token": "sometoken",
                                "poo": "iamchallenging",
                                "type": "url_verification"
                            }
                        """.trimIndent())
        )
                .andExpect(status().is4xxClientError)
    }

    @Test
    fun `Slack mentions receive 200s and send responses`() {
        mockMvc.perform(
                post("/slack")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("""
                            {
                                "token": "ZZZZZZWSxiZZZ2yIvs3peJ",
                                "team_id": "T061EG9R6",
                                "api_app_id": "A0MDYCDME",
                                "event": {
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
                            }
                        """.trimIndent())
        )
                .andExpect(status().isOk)
                .andExpect(content().json("{}"))

        assertThat(slackPoster.lastMessage?.text)
                .isEqualTo("Sorry m8, I'm being built rn")
        assertThat(slackPoster.lastMessage?.channel)
                .isEqualTo("C0LAN2Q65")
    }
}
