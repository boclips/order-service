package com.boclips.orders.domain.service.events

import com.boclips.eventbus.domain.video.VideoId
import com.boclips.eventbus.events.order.OrderItem
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.model.Price
import java.math.BigDecimal
import java.time.ZoneOffset
import com.boclips.eventbus.events.order.Order as EventOrder
import com.boclips.eventbus.events.order.OrderStatus as EventOrderStatus
import com.boclips.eventbus.events.order.OrderUser as EventOrderUser

class EventConverter {

    fun convertOrder(order: Order): EventOrder {
        return EventOrder.builder()
            .id(order.id.value)
            .legacyOrderId(order.legacyOrderId)
            .status(convertOrderStatus(order.status))
            .createdAt(order.createdAt.atZone(ZoneOffset.UTC))
            .updatedAt(order.updatedAt.atZone(ZoneOffset.UTC))
            .customerOrganisationName(order.organisation?.name ?: "UNKNOWN")
            .items(order.items.map { item ->
                OrderItem.builder()
                    .videoId(VideoId(item.video.videoServiceId.value))
                    .priceGbp(getItemPriceInGbp(item.price, order))
                    .build()
            })
            .authorisingUser(order.authorisingUser?.let(::convertOrderUser))
            .requestingUser(order.requestingUser.let(::convertOrderUser))
            .currency(order.currency)
            .isbnOrProductNumber(order.isbnOrProductNumber)
            .fxRateToGbp(order.fxRateToGbp)
            .isThroughPlatform(order.isThroughPlatform)
            .build()
    }

    fun convertOrderUser(user: OrderUser): EventOrderUser =
        when (user) {
            is OrderUser.CompleteUser ->
                EventOrderUser.builder()
                    .firstName(user.firstName)
                    .lastName(user.lastName)
                    .email(user.email)
                    .legacyUserId(user.legacyUserId)
                    .build()
            is OrderUser.BasicUser ->
                EventOrderUser.builder()
                    .label(user.label)
                    .build()
        }

    fun convertOrderStatus(orderStatus: OrderStatus): EventOrderStatus {
        return when (orderStatus) {
            OrderStatus.READY -> EventOrderStatus.COMPLETED
            OrderStatus.CANCELLED -> EventOrderStatus.CANCELLED
            OrderStatus.INCOMPLETED -> EventOrderStatus.INCOMPLETED
            OrderStatus.IN_PROGRESS -> EventOrderStatus.INCOMPLETED
            OrderStatus.INVALID -> EventOrderStatus.INVALID
        }
    }

    private fun getItemPriceInGbp(
        price: Price,
        order: Order
    ): BigDecimal {
        price.amount ?: return BigDecimal("0.00")
        order.fxRateToGbp ?: return BigDecimal("0.00")

        return price.amount.multiply(order.fxRateToGbp)
    }
}
