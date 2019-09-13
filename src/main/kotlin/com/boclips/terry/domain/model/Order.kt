package com.boclips.terry.domain.model

import com.boclips.terry.domain.exceptions.IllegalCurrencyException
import com.boclips.terry.domain.model.orderItem.OrderItem
import java.time.Instant
import java.util.Currency

class Order(
    val id: OrderId,
    val legacyOrderId: String,
    val status: OrderStatus,
    val authorisingUser: OrderUser,
    val requestingUser: OrderUser,
    val organisation: OrderOrganisation,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String,
    items: Iterable<OrderItem>
) {
    private val orderItems: MutableList<OrderItem> = mutableListOf()

    val items: List<OrderItem>
        get() = this.orderItems

    val currency: Currency?
        get() = this.orderItems.firstOrNull()?.price?.currency

    init {
        items.forEach { this.addItem(it) }
    }

    fun addItem(item: OrderItem) {
        val firstItem = orderItems.firstOrNull()
        if (firstItem == null || firstItem.price.currency == item.price.currency) {
            orderItems += item
        } else {
            throw IllegalCurrencyException("Currency: ${item.price.currency} is not the same as the current currency ${orderItems.first().price.currency} for order: $id")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (id != other.id) return false
        if (legacyOrderId != other.legacyOrderId) return false
        if (status != other.status) return false
        if (authorisingUser != other.authorisingUser) return false
        if (requestingUser != other.requestingUser) return false
        if (organisation != other.organisation) return false
        if (updatedAt != other.updatedAt) return false
        if (createdAt != other.createdAt) return false
        if (isbnOrProductNumber != other.isbnOrProductNumber) return false
        if (orderItems != other.orderItems) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + legacyOrderId.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + authorisingUser.hashCode()
        result = 31 * result + requestingUser.hashCode()
        result = 31 * result + organisation.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + isbnOrProductNumber.hashCode()
        result = 31 * result + orderItems.hashCode()
        return result
    }
}
