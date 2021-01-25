package com.boclips.orders.domain.model

import com.boclips.orders.domain.model.cart.TrimService

sealed class CartItemUpdateCommand {
    class SetTrimming(val trim: TrimService?) : CartItemUpdateCommand()
    class SetTranscriptRequested(val transcriptRequested: Boolean) : CartItemUpdateCommand()
    class SetCaptionsRequested(val captionsRequested: Boolean) : CartItemUpdateCommand()
    class SetEditRequest(val editRequest: String?) : CartItemUpdateCommand()
}
