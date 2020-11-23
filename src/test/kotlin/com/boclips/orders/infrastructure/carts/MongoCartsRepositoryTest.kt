package com.boclips.orders.infrastructure.carts

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory

class MongoCartsRepositoryTest : AbstractSpringIntegrationTest() {

    lateinit var mongoCartsRepository: MongoCartsRepository

    @Test
    fun `creates a cart`() {
        val cart = CartFactory.sample()

        val createdCart = mongoCartsRepository.create(cart)

        assertThat(createdCart).isEqualTo(cart)
    }
}
