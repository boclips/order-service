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
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerIntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `root path responds with a terrific message`() {
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
    fun `well-formed slack events receive 200s`() {
        mockMvc.perform(
                post("/slack")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content("""
                            {
                                "token": "XXYYZZ",
                                "team_id": "TXXXXXXXX",
                                "api_app_id": "AXXXXXXXXX",
                                "event": {
                                    "type": "name_of_event",
                                    "event_ts": "1234567890.123456",
                                    "user": "UXXXXXXX1"
                                },
                                "type": "event_callback",
                                "authed_users": [
                                    "UXXXXXXX1",
                                    "UXXXXXXX2"
                                ],
                                "event_id": "Ev08MFMKH6",
                                "event_time": 1234567890
                            }
                        """.trimIndent())
        )
                .andExpect(status().isOk)
                .andExpect(content().json("{}"))
    }
}
