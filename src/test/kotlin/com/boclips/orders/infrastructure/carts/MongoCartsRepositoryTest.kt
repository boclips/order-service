package com.boclips.orders.infrastructure.carts

import com.boclips.orders.domain.exceptions.CartItemNotFoundException
import com.boclips.orders.domain.model.CartItemUpdateCommand
import com.boclips.orders.domain.model.CartUpdateCommand
import com.boclips.orders.domain.model.cart.TrimService
import com.boclips.orders.domain.model.cart.UserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory

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
    fun `updates cart note`() {
        val userId = "publishers-user-id"

        val cart = CartFactory.sample(
            userId = userId,
            items = listOf()
        )

        mongoCartsRepository.create(cart)
        mongoCartsRepository.update(
            CartUpdateCommand.UpdateNote(
                userId = UserId(userId),
                note = "hi there"
            )
        )

        val updatedCart = mongoCartsRepository.findByUserId(UserId(userId))
        assertThat(updatedCart!!.note).isEqualTo("hi there")
    }

    @Test
    fun `updates existing cart item with additional service information`() {
        val userId = "publishers-user-id"

        val cart = CartFactory.sample(
            userId = userId,
            items = listOf()
        )
        val cartItem = CartFactory.cartItem(videoId = "video-id")
        val cartItem2 = CartFactory.cartItem(id = "other-id", videoId = "video-id2")

        mongoCartsRepository.create(cart)

        mongoCartsRepository.update(
            CartUpdateCommand.AddItem(
                userId = UserId(userId),
                cartItem = cartItem
            )
        )
        mongoCartsRepository.update(
            CartUpdateCommand.AddItem(
                userId = UserId(userId),
                cartItem = cartItem2
            )
        )

        mongoCartsRepository.updateCartItem(
            userId = UserId(userId),
            cartItemId = cartItem.id,
            updateCommands = listOf(
                CartItemUpdateCommand.SetTrimming(
                    trim = TrimService(
                        from = "6:66",
                        to = "6:69"
                    )
                )
            )
        )

        val updatedCart = mongoCartsRepository.findByUserId(UserId(userId))

        assertThat(updatedCart?.items).hasSize(2)
        assertThat(updatedCart?.items?.first()?.videoId?.value).isEqualTo("video-id")
        assertThat(updatedCart?.items?.first()?.additionalServices?.trim?.from).isEqualTo("6:66")
        assertThat(updatedCart?.items?.first()?.additionalServices?.trim?.to).isEqualTo("6:69")
    }

    @Test
    fun `updates existing cartItem's additional services with transcript request`() {
        createCart(
            userId = "publishers-user-id",
            items = listOf(
                CartFactory.cartItem(
                    id = "cart-item-1",
                    additionalServices = CartFactory.additionalServices(transcriptRequested = false)
                )
            )
        )

        mongoCartsRepository.updateCartItem(
            userId = UserId("publishers-user-id"),
            cartItemId = "cart-item-1",
            updateCommands = listOf(CartItemUpdateCommand.SetTranscriptRequested(true))
        )

        val updatedCart = mongoCartsRepository.findByUserId(UserId("publishers-user-id"))

        assertThat(updatedCart?.items).hasSize(1)
        assertThat(updatedCart?.items?.first()?.additionalServices?.transcriptRequested).isEqualTo(true)
        assertThat(updatedCart?.items?.first()?.additionalServices?.trim).isNotNull
    }

    @Test
    fun `updates existing cartItem's additional services with captions request`() {
        createCart(
            userId = "publishers-user-id",
            items = listOf(
                CartFactory.cartItem(
                    id = "cart-item-1",
                    additionalServices = CartFactory.additionalServices(captionsRequested = false)
                )
            )
        )

        mongoCartsRepository.updateCartItem(
            userId = UserId("publishers-user-id"),
            cartItemId = "cart-item-1",
            updateCommands = listOf(CartItemUpdateCommand.SetCaptionsRequested(true))
        )

        val updatedCart = mongoCartsRepository.findByUserId(UserId("publishers-user-id"))

        assertThat(updatedCart?.items).hasSize(1)
        assertThat(updatedCart?.items?.first()?.additionalServices?.captionsRequested).isEqualTo(true)
        assertThat(updatedCart?.items?.first()?.additionalServices?.trim).isNotNull
    }

    @Test
    fun `updates existing cartItem's additional services with editing requested`() {
        createCart(
            userId = "publishers-user-id",
            items = listOf(
                CartFactory.cartItem(
                    id = "cart-item-1",
                    additionalServices = CartFactory.additionalServices(editingRequested = null)
                )
            )
        )

        mongoCartsRepository.updateCartItem(
            userId = UserId("publishers-user-id"),
            cartItemId = "cart-item-1",
            updateCommands = listOf(CartItemUpdateCommand.SetEditingRequested("yes please"))
        )

        val updatedCart = mongoCartsRepository.findByUserId(UserId("publishers-user-id"))

        assertThat(updatedCart?.items).hasSize(1)
        assertThat(updatedCart?.items?.first()?.additionalServices?.editingRequested).isEqualTo("yes please")
    }

    @Test
    fun `throws item doesn't exist exception`() {
        val userId = "publishers-user-id"

        val cart = CartFactory.sample(
            userId = userId,
            items = listOf()
        )

        mongoCartsRepository.create(cart)

        assertThrows<CartItemNotFoundException> {
            mongoCartsRepository.updateCartItem(
                userId = UserId(userId),
                cartItemId = "1234",
                updateCommands = listOf(
                    CartItemUpdateCommand.SetTrimming(
                        trim = TrimService(
                            from = "6:66",
                            to = "6:69"
                        )
                    )
                )
            )
        }
            .message.let {
                assertThat(it).contains("Could not find cart item with ID: 1234 for user: publishers-user-id")
            }
    }

    @Test
    fun `empties cart`() {
        val cart =
            CartFactory.sample(userId = "publishers-user-id", items = listOf(CartFactory.cartItem()), note = "hello")
        mongoCartsRepository.create(cart)

        mongoCartsRepository.update(
            CartUpdateCommand.EmptyCart(
                userId = cart.userId
            )
        )

        val emptiedCart = mongoCartsRepository.findByUserId(UserId("publishers-user-id"))
        assertThat(emptiedCart?.items).isEmpty()
        assertThat(emptiedCart?.note).isNull()
    }

    @Test
    fun `can delete a cart item`() {
        val cart = CartFactory.sample(userId = "publishers-user-id", items = listOf(CartFactory.cartItem()))
        val cartItem = CartFactory.cartItem(videoId = "video-id")
        mongoCartsRepository.create(cart)

        mongoCartsRepository.update(
            CartUpdateCommand.AddItem(
                userId = cart.userId,
                cartItem = cartItem
            )
        )

        mongoCartsRepository.deleteItem(userId = cart.userId, cartItemId = cartItem.id)

        assertThat(mongoCartsRepository.findByUserId(UserId("publishers-user-id"))?.items).isEmpty()
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
