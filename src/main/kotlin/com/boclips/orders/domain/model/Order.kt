package com.boclips.orders.domain.model

import com.boclips.orders.common.sumByBigDecimal
import com.boclips.orders.domain.exceptions.IllegalCurrencyException
import com.boclips.orders.domain.model.orderItem.OrderItem
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.time.Instant
import java.util.Currency

class Order(
    val id: OrderId,
    val legacyOrderId: String,
    val status: OrderStatus,
    val authorisingUser: OrderUser?,
    val requestingUser: OrderUser,
    val organisation: OrderOrganisation?,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isbnOrProductNumber: String?,
    val isThroughPlatform: Boolean,
    items: Iterable<OrderItem>
) {
    companion object {
        fun builder() = Builder()
    }

    private val orderItems: MutableList<OrderItem> = mutableListOf()

    val items: List<OrderItem>
        get() = this.orderItems

    val currency: Currency?
        get() = this.orderItems.firstOrNull()?.price?.currency

    val totalPrice: BigDecimal
        get() = this.orderItems.sumByBigDecimal { it.price.amount ?: BigDecimal.ZERO}

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
        if (isThroughPlatform != other.isThroughPlatform) return false
        if (orderItems != other.orderItems) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + legacyOrderId.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + (authorisingUser?.hashCode() ?: 0)
        result = 31 * result + requestingUser.hashCode()
        result = 31 * result + (organisation?.hashCode() ?: 0)
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (isbnOrProductNumber?.hashCode() ?: 0)
        result = 31 * result + isThroughPlatform.hashCode()
        result = 31 * result + orderItems.hashCode()
        return result
    }

    class Builder {
        private lateinit var legacyOrderId: String
        private lateinit var status: OrderStatus
        private lateinit var requestingUser: OrderUser
        private lateinit var updatedAt: Instant
        private lateinit var createdAt: Instant
        private lateinit var items: List<OrderItem>

        private var isThroughPlatform: Boolean = true

        private var authorisingUser: OrderUser? = null
        private var organisation: OrderOrganisation? = null
        private var isbnOrProductNumber: String? = null

        fun legacyOrderId(legacyOrderId: String) = apply { this.legacyOrderId = legacyOrderId }
        fun status(status: OrderStatus) = apply { this.status = status }
        fun authorisingUser(authorisingUser: OrderUser?) = apply { this.authorisingUser = authorisingUser }
        fun requestingUser(requestingUser: OrderUser) = apply { this.requestingUser = requestingUser }
        fun organisation(organisation: OrderOrganisation?) = apply { this.organisation = organisation }
        fun updatedAt(updatedAt: Instant) = apply { this.updatedAt = updatedAt }
        fun createdAt(createdAt: Instant) = apply { this.createdAt = createdAt }
        fun isbnOrProductNumber(isbnOrProductNumber: String?) = apply { this.isbnOrProductNumber = isbnOrProductNumber }
        fun items(items: List<OrderItem>) = apply { this.items = items }
        fun isThroughPlatform(orderThroughPlatform: Boolean) =
            apply { this.isThroughPlatform = orderThroughPlatform }

        fun build(): Order = Order(
            id = OrderId(ObjectId().toHexString()),
            legacyOrderId = legacyOrderId,
            status = status,
            authorisingUser = authorisingUser,
            requestingUser = requestingUser,
            organisation = organisation,
            updatedAt = updatedAt,
            createdAt = createdAt,
            isbnOrProductNumber = isbnOrProductNumber,
            isThroughPlatform = isThroughPlatform,
            items = items
        )
    }
}