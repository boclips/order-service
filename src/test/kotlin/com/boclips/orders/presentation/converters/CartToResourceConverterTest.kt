package com.boclips.orders.presentation.converters

import com.boclips.orders.domain.model.cart.AdditionalServices
import com.boclips.orders.domain.model.cart.TrimService
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import testsupport.CartFactory

class CartToResourceConverterTest {

    @Test
    fun `should convert to resource`() {
        val cart = CartFactory.sample(
            items = listOf(
                CartFactory.cartItem(
                    id = "cart-item-1",
                    videoId = "video-1",
                    additionalServices = AdditionalServices(
                        trim = TrimService(from = "1:00", to = "2:00")
                    )
                ),
                CartFactory.cartItem(
                    id = "cart-item-2",
                    videoId = "video-2",
                    additionalServices = null
                )
            )
        )

        val cartResource = CartToResourceConverter.convert(cart)
        Assertions.assertThat(cartResource.items).hasSize(2)
        Assertions.assertThat(cartResource.items[0].content?.id).isEqualTo("cart-item-1")
        Assertions.assertThat(cartResource.items[0].content?.videoId).isEqualTo("video-1")
        Assertions.assertThat(cartResource.items[0].content?.additionalServices!!.trim!!.from).isEqualTo("1:00")
        Assertions.assertThat(cartResource.items[0].content?.additionalServices!!.trim!!.to).isEqualTo("2:00")
        Assertions.assertThat(cartResource.items[1].content?.id).isEqualTo("cart-item-2")
        Assertions.assertThat(cartResource.items[1].content?.videoId).isEqualTo("video-2")
        Assertions.assertThat(cartResource.items[1].content?.additionalServices).isNull()
    }
}
