package com.boclips.terry

import org.assertj.core.api.Assertions.assertThat
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
    fun `responds with a terrific message`() {
        val response = mockMvc.perform(get("/")).andReturn().response

        assertThat(response.contentAsString).contains("Do as I say")
        assertThat(response.status).isEqualTo(200)
    }

    @Test
    fun `can meet Slack's verification challenge`() {
        mockMvc.perform(
                post("/slack-verification")
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
}
