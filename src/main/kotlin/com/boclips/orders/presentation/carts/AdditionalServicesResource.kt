package com.boclips.orders.presentation.carts

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AdditionalServicesResource(
    val trim: TrimServiceResource?
)

data class TrimServiceResource(
    val from: String,
    val to: String
)
