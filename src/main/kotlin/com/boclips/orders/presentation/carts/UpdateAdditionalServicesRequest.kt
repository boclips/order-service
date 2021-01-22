package com.boclips.orders.presentation.carts

import com.boclips.orders.common.Specifiable

data class UpdateAdditionalServicesRequest(
    val trim: Specifiable<TrimServiceRequest>? = null,
    val transcriptRequested: Specifiable<Boolean>? = null
)

