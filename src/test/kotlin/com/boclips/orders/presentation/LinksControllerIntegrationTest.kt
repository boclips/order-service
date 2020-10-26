package com.boclips.orders.presentation

import org.hamcrest.core.StringEndsWith.endsWith
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest
import testsupport.asHQStaff
import testsupport.asNonHQStaff

class LinksControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Test
    fun `anonymous user doesn't see orders link`() {
        mockMvc.perform(get("/v1/"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.orders").doesNotExist())
    }

    @Test
    fun `hq staff have access to an orders link`() {
        mockMvc.perform(get("/v1/").asHQStaff())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links.orders").exists())
            .andExpect(jsonPath("$._links.orders.href", endsWith("/orders")))
            .andExpect(jsonPath("$._links.order").exists())
            .andExpect(jsonPath("$._links.order.href", endsWith("/orders/{id}")))
            .andExpect(
                jsonPath(
                    "$._links.exportOrders.href",
                    endsWith("/orders{?usd,eur,sgd,aud,cad}")
                )
            )
    }

    @Test
    fun `valid without correct roles gets an empty link`() {
        mockMvc.perform(get("/v1/").asNonHQStaff())
            .andExpect(status().isOk)
            .andExpect(jsonPath("$._links").exists())
    }
}
