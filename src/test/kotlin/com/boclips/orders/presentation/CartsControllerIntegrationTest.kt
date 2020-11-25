package com.boclips.orders.presentation

import com.boclips.orders.domain.model.cart.UserId
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasLength
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory
import testsupport.asPublisher

class CartsControllerIntegrationTest : AbstractSpringIntegrationTest() {

    @Nested
    inner class GetCart {
        @Test
        fun `creates and returns a cart if did not exist before`() {
            val userId = "publishers-user-id"

            mockMvc.perform(
                get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items", hasSize<Any>(0)))
                .andExpect(jsonPath("$._links", hasKey("self")))
                .andExpect(jsonPath("$._links", hasKey("addItem")))

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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items", hasSize<Any>(1)))
                .andExpect(jsonPath("$.items[0]._links.self.href", endsWith("/cart-item-id")))
                .andExpect(jsonPath("$._links.self.href", endsWith("/cart")))
                .andExpect(jsonPath("$._links.addItem.href", endsWith("/cart/items")))
        }
    }

    @Nested
    inner class AddItemToCart {
        @Test
        fun `can create cart item`() {
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
                .andExpect(jsonPath("$.videoId", equalTo("video-id-1")))
                .andExpect(jsonPath("$.id", Matchers.not(emptyString())))
                .andExpect(MockMvcResultMatchers.header().exists("Location"))

            mockMvc.perform(get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.items[0].videoId", equalTo("video-id-1")))
                .andExpect(jsonPath("$.items[0].id", Matchers.not(emptyString())))
        }
    }
}
