package com.boclips.terry

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class TerryApplicationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `responds with a terrific message`() {
        val response = mockMvc.perform(get("/")).andReturn().response

        assertThat(response.contentAsString).contains("Do as I say")
        assertThat(response.status).isEqualTo(200)
    }

}
