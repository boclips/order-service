package com.boclips.terry.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class OrdersControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `user without permission to view orders is forbidden from listing orders`() {
        mockMvc.perform(
            get("/v1/orders")
        )
            .andExpect(status().isForbidden)
    }
}
