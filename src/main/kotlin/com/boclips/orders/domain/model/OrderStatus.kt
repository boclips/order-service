package com.boclips.orders.domain.model

import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "status",
    visible = true
)
enum class OrderStatus {
    READY,
    INCOMPLETED,
    IN_PROGRESS,
    CANCELLED,
    INVALID;
}
