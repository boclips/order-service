package com.boclips.orders.presentation

import java.math.BigDecimal
import javax.validation.Valid

data class UpdateOrderItemRequest(
    var price: BigDecimal? = null,

    @field:Valid
    var license: LicenseRequest? = null
)
