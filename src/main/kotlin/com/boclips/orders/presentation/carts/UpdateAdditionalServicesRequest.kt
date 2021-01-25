package com.boclips.orders.presentation.carts

import com.boclips.orders.common.Specifiable
import com.boclips.orders.common.ValidateTrimming

data class UpdateAdditionalServicesRequest(
    @ValidateTrimming
    val trim: Specifiable<TrimServiceRequest>? = null,
    val transcriptRequested: Specifiable<Boolean>? = null,
    val captionsRequested: Specifiable<Boolean>? = null,
    val editingRequested: Specifiable<String>? = null
)

