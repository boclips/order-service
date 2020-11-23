package com.boclips.orders.presentation

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest

class CartItemsIntegrationTest : AbstractSpringIntegrationTest() {
    // @Autowired
    // lateinit var createCartItem: CreateCartItem

    @Test
    fun `can create and retrieve cart items`() {
        val userId = "123abc456"

        val location = mockMvc.perform(
            post("/v1/users/$userId/cart/items").contentType(MediaType.APPLICATION_JSON).content(
                """
                    {
                        "videoId": "video-id-1",
                    }
                """.trimIndent()
            )
        ).andExpect(status().isCreated).andReturn().response.getHeader("location")

        mockMvc.perform(get(location!!)).andExpect(
            jsonPath("$.items[0].videoId", equalTo("video-id-1"))
        ).andExpect(
            jsonPath("$.items[0].id").exists()
        )
    }
}
