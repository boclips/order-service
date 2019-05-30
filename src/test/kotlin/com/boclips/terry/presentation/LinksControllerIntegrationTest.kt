package com.boclips.terry.presentation

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.hamcrest.core.StringEndsWith.endsWith
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.asBackofficeStaff
import testsupport.asNonBackOfficeStaff

class LinksControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `anonymous user doesn't see orders link`() {
        mockMvc.perform(get("/v1/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.orders").doesNotExist())
    }

    @Test
    fun `backoffice staff do see an orders link`() {
        mockMvc.perform(get("/v1/").asBackofficeStaff())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.orders").exists())
            .andExpect(jsonPath("$._links.orders.href", endsWith("/orders")))
    }

    @Test
    fun `valid without correct roles gets an empty link`() {
        mockMvc.perform(get("/v1/").asNonBackOfficeStaff())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links").exists())
    }
}
