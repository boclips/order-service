package com.boclips.terry.domain.model

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "status",
    visible = true
)
enum class OrderStatus {
    COMPLETED,
    CONFIRMED,
    CANCELLED,
    OPEN,
    PROCESSING,
    INVALID;
}
