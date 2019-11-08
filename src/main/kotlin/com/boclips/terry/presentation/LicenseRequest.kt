package com.boclips.terry.presentation

import javax.validation.constraints.NotBlank

data class LicenseRequest(
    @field:NotBlank
    var territory: String?,
    @field:NotBlank
    var duration: String?
)
