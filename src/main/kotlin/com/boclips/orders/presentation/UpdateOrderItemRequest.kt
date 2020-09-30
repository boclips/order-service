package com.boclips.orders.presentation

import java.math.BigDecimal

data class UpdateOrderItemRequest(
    var price: BigDecimal? = null,

    var license: LicenseRequest? = null
)
