package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.model.CartUpdateCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory
import java.lang.IllegalStateException

class MongoCartsRepositoryTest : AbstractSpringIntegrationTest() {

    @Test
    fun `creates a cart`() {
        val cart = CartFactory.sample(items = listOf(CartFactory.cartItem(videoId = "item-one")))

        val createdCart = mongoCartsRepository.create(cart)

        assertThat(createdCart).isEqualTo(cart)
    }

    @Test
    fun `finds cart by userId`() {
        val cart = CartFactory.sample(items = listOf(CartFactory.cartItem(videoId = "item-one")))

        mongoCartsRepository.create(cart)

        val cartByUserId = mongoCartsRepository.findByUserId(cart.userId)

        assertThat(cartByUserId).isEqualTo(cart)
    }

    @Test
    fun `finds all carts`() {
        mongoCartsRepository.create(CartFactory.sample(userId = "user-one"))
        mongoCartsRepository.create(CartFactory.sample(userId = "user-two"))

        val carts = mongoCartsRepository.findAll()

        assertThat(carts).hasSize(2)
    }

    @Test
    fun `deletes all carts`() {
        mongoCartsRepository.create(CartFactory.sample(userId = "user-one"))
        mongoCartsRepository.create(CartFactory.sample(userId = "user-two"))

        mongoCartsRepository.deleteAll()

        val carts = mongoCartsRepository.findAll()
        assertThat(carts).hasSize(0)
    }

    @Test
    fun `adds new items to cart`() {
        val cart = CartFactory.sample(userId = "publishers-user-id", items = listOf())
        mongoCartsRepository.create(cart)

        val updatedCart = mongoCartsRepository.update(
            CartUpdateCommand.AddItem(
                userId = cart.userId,
                cartItem = CartFactory.cartItem(videoId = "video-id")
            )
        )

        assertThat(updatedCart.items).hasSize(1)
        assertThat(updatedCart.items.first().videoId.value).isEqualTo("video-id")
    }

    @Test
    fun `throws exception when cart does not exist`() {
        val cart = CartFactory.sample(userId = "publishers-user-id", items = listOf())

        assertThrows<IllegalStateException> {
            mongoCartsRepository.update(
                CartUpdateCommand.AddItem(
                    userId = cart.userId,
                    cartItem = CartFactory.cartItem(videoId = "video-id")
                )
            )
        }
            .message.let {
                assertThat(it).contains("Adding cart items: cart does not exist for user")
            }
    }
}
