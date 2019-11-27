package com.boclips.orders.presentation

import javax.validation.constraints.NotBlank

data class LicenseRequest(
    @field:NotBlank
    var territory: String?,
    @field:NotBlank
    var duration: String?
)
