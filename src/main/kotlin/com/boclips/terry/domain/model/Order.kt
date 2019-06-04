package com.boclips.terry.domain.model

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

    //TODO this should be moved out of the domain
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
    val id: OrderId,
    val uuid: String,
    val status: OrderStatus,
    val vendorEmail: String,
    val creatorEmail: String,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String,
    val items: List<OrderItem>
)

