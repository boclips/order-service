package com.boclips.orders.infrastructure.carts

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.CartFactory

class CartDocumentConverterTest {

    @Test
    fun `converts cart to and from document`() {
        val originalCart = CartFactory.sample(items = CartFactory.cartItems(listOf("1", "2")))

        val cartDocument = CartDocumentConverter.toCartDocument(originalCart)
        val cart = CartDocumentConverter.toCart(cartDocument)

        assertThat(cart).isEqualTo(originalCart)
    }
}
