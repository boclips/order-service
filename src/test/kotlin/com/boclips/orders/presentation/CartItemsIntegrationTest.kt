package com.boclips.orders.presentation

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest
import testsupport.asPublisher

class CartItemsIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `can create and retrieve cart items`() {
        val userId = "publishers-user-id"
        createCart(userId)

        mockMvc.perform(
            post("/v1/users/$userId/cart/items").contentType(MediaType.APPLICATION_JSON).content(
                """
                    {
                        "videoId": "video-id-1"
                    }
                """.trimIndent()
            ).asPublisher(userId)
        )
            .andExpect(status().isCreated)
            .andExpect(
                jsonPath("$.items[0].videoId", equalTo("video-id-1"))
            )
    }
}
