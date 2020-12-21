package com.boclips.orders.application.orders.converters.csv

import com.boclips.orders.application.orders.exceptions.IncompleteUserData
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.TrimRequest
import com.boclips.orders.domain.model.video.VideoId
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.orders.presentation.PlaceOrderRequest
import com.boclips.orders.presentation.PlaceOrderRequestItem
import com.boclips.orders.presentation.PlaceOrderRequestUser
import mu.KLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Currency

@Component
class OrderFromRequestConverter(val videoProvider: VideoProvider) {
    companion object : KLogging()

    fun toOrder(request: PlaceOrderRequest): Order {
        val user = toOrderUser(request.user)
        val items = request.items.map { toOrderItem(it) }
        return Order.builder()
            .currency(getCommonCurrencyOf(items))
            .authorisingUser(user)
            .requestingUser(user)
            .organisation(OrderOrganisation(name = request.user.organisation.name))
            .items(items)
            .status(OrderStatus.INCOMPLETED)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .isThroughPlatform(true)
            .isbnOrProductNumber(null)
            .fxRateToGbp(null)
            .build()
    }

    private fun getCommonCurrencyOf(items: List<OrderItem>): Currency? {
        return if (items.isNotEmpty() && items.map { it.price.currency }.all { it == items.first().price.currency })
            items.first().price.currency
        else null
    }

    private fun toOrderUser(userRequest: PlaceOrderRequestUser) =
        takeIf { isUserDataValid(userRequest) }?.let {
            OrderUser.CompleteUser(
                firstName = userRequest.firstName,
                lastName = userRequest.lastName,
                email = userRequest.email,
                userId = userRequest.id
            )
        } ?: throw IncompleteUserData()

    fun toOrderItem(itemRequest: PlaceOrderRequestItem): OrderItem {
        return videoProvider.get(VideoId(value = itemRequest.videoId)).let {
            OrderItem.builder()
                .video(it)
                .price(it.price!!)
                .transcriptRequested(false)
                .trim(TrimRequest.NoTrimming)
                .id(itemRequest.id)
                .build()
        }
    }

    private fun isUserDataValid(userRequest: PlaceOrderRequestUser): Boolean =
        userRequest.firstName.isNotBlank() &&
            userRequest.lastName.isNotBlank() &&
            userRequest.email.isNotBlank() &&
            userRequest.id.isNotBlank()
}
