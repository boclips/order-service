package com.boclips.terry.domain

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.Instant

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
    PROCESSING;

    companion object {
        fun parse(input: String): OrderStatus =
            when (input) {
                "COMPLETED" ->
                    COMPLETED
                "CONFIRMED" ->
                    CONFIRMED
                "CANCELLED" ->
                    CANCELLED
                else ->
                    PROCESSING
            }
    }
}

data class Order(
    val id: String,
    val uuid: String,
    val status: OrderStatus,
    val vendorEmail: String,
    val creatorEmail: String,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String
)
