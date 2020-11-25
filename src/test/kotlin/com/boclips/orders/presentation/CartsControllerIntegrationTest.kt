package com.boclips.orders.presentation

import com.boclips.orders.domain.model.cart.UserId
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory
import testsupport.asPublisher

class CartsControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Test
    fun `creates and returns a cart if did not exist before`() {
        val userId = "publishers-user-id"

        mockMvc.perform(
            get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo(userId)))
            .andExpect(jsonPath("$.items", hasSize<Any>(0)))
            .andExpect(jsonPath("$._links", hasSize<Any>(2)))

        assertThat(mongoCartsRepository.findByUserId(UserId(userId))).isNotNull
    }

    @Test
    fun `returns cart when already exists`() {
        val userId = "publishers-user-id"
        createCart(userId)

        mockMvc.perform(
            get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo(userId)))
            .andExpect(jsonPath("$.items", hasSize<Any>(0)))

        assertThat(mongoCartsRepository.findAll()).hasSize(1)
    }

    @Test
    fun `checks all links in cart`() {
        val userId = "publishers-user-id"
        createCart(userId, listOf(CartFactory.cartItem(id = "cart-item-id")))

        mockMvc.perform(
            get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo(userId)))
            .andExpect(jsonPath("$.items", hasSize<Any>(1)))
            .andExpect(jsonPath("$.items[0]._links[0].rel", equalTo("self")))
            .andExpect(jsonPath("$.items[0]._links[0].href", endsWith("/cart-item-id")))
            .andExpect(jsonPath("$._links[0].rel", equalTo("self")))
            .andExpect(jsonPath("$._links[0].href", endsWith("/cart")))
            .andExpect(jsonPath("$._links[1].rel", equalTo("addItem")))
            .andExpect(jsonPath("$._links[1].href", endsWith("/cart/items")))
    }

    @Test
    fun `can create and retrieve cart items`() {
        val userId = "publishers-user-id"
        createCart(userId)

        mockMvc.perform(
            post("/v1/cart/items").contentType(MediaType.APPLICATION_JSON).content(
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

        mockMvc.perform(get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.userId", equalTo(userId)))
        jsonPath("$.items[0].videoId", equalTo("video-id-1"))
    }
}
