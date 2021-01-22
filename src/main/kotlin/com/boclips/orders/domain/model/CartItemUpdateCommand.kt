package com.boclips.orders.domain.model

import com.boclips.orders.domain.model.cart.TrimService

sealed class CartItemUpdateCommand {
    class ReplaceTrimming(val trim: TrimService?) : CartItemUpdateCommand()
}
