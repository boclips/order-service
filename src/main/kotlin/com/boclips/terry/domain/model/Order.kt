package com.boclips.terry.domain.model

import com.boclips.terry.domain.exceptions.IllegalCurrencyException
import com.boclips.terry.domain.model.orderItem.OrderItem
import org.bson.types.ObjectId
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

    class Builder {
        private lateinit var legacyOrderId: String
        private lateinit var status: OrderStatus
        private lateinit var requestingUser: OrderUser
        private lateinit var updatedAt: Instant
        private lateinit var createdAt: Instant
        private lateinit var items: List<OrderItem>

        private var authorisingUser: OrderUser? = null
        private var organisation: OrderOrganisation? = null
        private var isbnOrProductNumber: String? = null

        fun legacyOrderId(legacyOrderId: String): Builder {
            this.legacyOrderId = legacyOrderId
            return this
        }

        fun status(status: OrderStatus): Builder {
            this.status = status
            return this
        }

        fun authorisingUser(authorisingUser: OrderUser?): Builder {
            this.authorisingUser = authorisingUser
            return this
        }

        fun requestingUser(requestingUser: OrderUser): Builder {
            this.requestingUser = requestingUser
            return this
        }

        fun organisation(organisation: OrderOrganisation?): Builder {
            this.organisation = organisation
            return this
        }

        fun updatedAt(updatedAt: Instant): Builder {
            this.updatedAt = updatedAt
            return this
        }

        fun createdAt(createdAt: Instant): Builder {
            this.createdAt = createdAt
            return this
        }

        fun isbnOrProductNumber(isbnOrProductNumber: String?): Builder {
            this.isbnOrProductNumber = isbnOrProductNumber
            return this
        }

        fun items(items: List<OrderItem>): Builder {
            this.items = items
            return this
        }

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
            items = items
        )
    }
}
