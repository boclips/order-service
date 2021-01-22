package com.boclips.orders.presentation

import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.TrimService
import com.boclips.orders.domain.model.cart.UserId
import com.nhaarman.mockito_kotlin.isNull
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.from
import org.hamcrest.Matchers
import org.hamcrest.Matchers.emptyString
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasKey
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
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
    inner class CartOperations {
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

        @Test
        fun `can delete cart item`() {
            val userId = "publishers-user-id"
            createCart(userId)

            val cartItemUrl = mockMvc.perform(
                post("/v1/cart/items").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                        "videoId": "video-id-1"
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andReturn().response.getHeaderValue("location")

            mockMvc.perform(
                delete("$cartItemUrl").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
            )
                .andExpect(status().isNoContent)

            mockMvc.perform(
                get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
            ).andExpect(jsonPath("$.items", hasSize<Any>(0)))
        }

        @Test
        fun `should return NOT FOUND if cart id is not found`() {
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

            mockMvc.perform(
                delete("/v1/cart/items/madeup-cart-id").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
            )
                .andExpect(status().isNotFound)
        }

        @Test
        fun `can't delete someone else's cart item`() {
            val userId = "publishers-user-id"
            val otherUserId = "other-publishers-user-id"

            createCart(userId)

            val cartItemUrl = mockMvc.perform(
                post("/v1/cart/items").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                        "videoId": "video-id-1"
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andReturn().response.getHeaderValue("location")

            mockMvc.perform(
                delete("$cartItemUrl").contentType(MediaType.APPLICATION_JSON).asPublisher(otherUserId)
            )
                .andExpect(status().isNotFound)

            mockMvc.perform(
                get("/v1/cart").contentType(MediaType.APPLICATION_JSON).asPublisher(userId)
            ).andExpect(jsonPath("$.items", hasSize<Any>(1)))
        }

        @Test
        fun `can update cart with a note`() {
            val userId = "publishers-user-id"

            createCart(userId)

            mockMvc
                .perform(
                    patch("/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {
                    "note" : "This is a note"
                    }
                            """.trimIndent()
                        )
                        .asPublisher(userId)
                )
                .andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.note", equalTo("This is a note")))
        }

        @Test
        fun `can update cart with an empty note`() {
            val userId = "publishers-user-id"

            createCart(userId)

            mockMvc
                .perform(
                    patch("/v1/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            """
                    {
                    "note" : ""
                    }
                            """.trimIndent()
                        )
                        .asPublisher(userId)
                )
                .andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.note", equalTo("")))
        }

        @Test
        fun `can update cart item with addition services`() {
            val userId = "publishers-user-id"

            createCart(userId)

            val cartItemId = saveItemToCart(videoId = "video-id", userId = userId)

            mockMvc.perform(
                patch("/v1/cart/items/${cartItemId.id}").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                    "trim" : {
                        "from": "1:23",
                        "to": "2:69"
                        }
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.items[0].videoId", equalTo("video-id")))
                .andExpect(jsonPath("$.items[0].additionalServices").exists())
                .andExpect(jsonPath("$.items[0].additionalServices.trim").exists())
                .andExpect(jsonPath("$.items[0].additionalServices.trim.from", equalTo("1:23")))
                .andExpect(jsonPath("$.items[0].additionalServices.trim.to", equalTo("2:69")))
                .andExpect(jsonPath("$.items[0].id", Matchers.not(emptyString())))
        }

        @Test
        fun `can update additional services using more specific URL`() {
            val userId = "publishers-user-id"

            createCart(userId)

            val cartItemId = saveItemToCart(videoId = "video-id", userId = userId)

            mockMvc.perform(
                patch("/v1/cart/items/${cartItemId.id}/additional-services").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                    "trim" : {
                        "from": "1:23",
                        "to": "2:69"
                        }
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.items[0].videoId", equalTo("video-id")))
                .andExpect(jsonPath("$.items[0].additionalServices").exists())
                .andExpect(jsonPath("$.items[0].additionalServices.trim").exists())
                .andExpect(jsonPath("$.items[0].additionalServices.trim.from", equalTo("1:23")))
                .andExpect(jsonPath("$.items[0].additionalServices.trim.to", equalTo("2:69")))
                .andExpect(jsonPath("$.items[0].id", Matchers.not(emptyString())))
        }

        @Test
        fun `can set additional services trim to null`() {
            val userId = "publishers-user-id"

            createCart(userId)

            mongoCartsRepository.update(
                CartUpdateCommand.AddItem(
                    userId = UserId(userId),
                    cartItem = CartFactory.cartItem(
                        id = "cart-item-1",
                        additionalServices = AdditionalServices(
                            trim = TrimService(
                                from = "1:00", to = "2:00"
                            )
                        )
                    )
                )
            )

            mockMvc.perform(
                patch("/v1/cart/items/cart-item-1").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                    "trim" : null
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andExpect(status().is2xxSuccessful)
                .andExpect(jsonPath("$.items[0].videoId", equalTo("video-id")))
                .andExpect(jsonPath("$.items[0].additionalServices.trim").doesNotExist())
                .andExpect(jsonPath("$.items[0].id", Matchers.not(emptyString())))
        }

        @Test
        fun `does not touch trimming when not included in request`() {
            val userId = "publishers-user-id"

            createCart(
                userId = userId,
                items = listOf(
                    CartFactory.cartItem(
                        id = "1",
                        additionalServices = AdditionalServices(trim = TrimService(from = "0:00", to = "1:00"))
                    )
                )
            )

            mockMvc.perform(
                patch("/v1/cart/items/1").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andExpect(status().is2xxSuccessful)
                .andDo(MockMvcResultHandlers.print())
                .andExpect(jsonPath("$.items[0].videoId", equalTo("video-id")))
                .andExpect(jsonPath("$.items[0].additionalServices.trim").exists())
                .andExpect(jsonPath("$.items[0].id", Matchers.not(emptyString())))
        }

        @Test
        fun `get 404 when updating a missing cart item`() {
            val userId = "publishers-user-id"

            createCart(userId)

            saveItemToCart(videoId = "video-id", userId = userId)

            mockMvc.perform(
                patch("/v1/cart/items/1234").contentType(MediaType.APPLICATION_JSON).content(
                    """
                    {
                    "trim" : {
                        "from": "1:23",
                        "to": "2:69"
                        }
                    }
                    """.trimIndent()
                ).asPublisher(userId)
            ).andExpect(status().isNotFound)
        }
    }
}
