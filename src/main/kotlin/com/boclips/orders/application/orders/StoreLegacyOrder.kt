package com.boclips.orders.application.orders

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.orders.application.exceptions.LegacyOrderProcessingException
import com.boclips.orders.application.orders.converters.legacy.OrderStatusConverter
import com.boclips.orders.application.orders.converters.parseTrimRequest
import com.boclips.orders.domain.model.LegacyOrdersRepository
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.OrderId
import com.boclips.orders.domain.model.OrderOrganisation
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrderUser
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.OrderItem
import com.boclips.orders.domain.model.orderItem.VideoId
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.domain.service.VideoProvider
import com.boclips.orders.infrastructure.orders.LegacyOrderDocument
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class StoreLegacyOrder(
    private val repo: OrdersRepository,
    private val orderService: OrderService,
    private val legacyOrdersRepository: LegacyOrdersRepository,
    private val videoProvider: VideoProvider
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {
        try {
            val order = convertLegacyOrder(event)

            // Ideally update and create would be two different events so we wouldn't have to distinguish them here
            val foundOrder = repo.findOneByLegacyId(order.legacyOrderId)
            if (foundOrder != null) {
                orderService.update(
                    OrderUpdateCommand.ReplaceStatus(orderId = foundOrder.id, orderStatus = order.status)
                )
            } else {
                repo.save(order = order)
            }

            legacyOrdersRepository.add(
                LegacyOrderDocument(
                    order = event.order,
                    items = event.orderItems,
                    requestingUser = event.requestingUser,
                    authorisingUser = event.authorisingUser
                )
            )
        } catch (e: Exception) {
            logger.error { "Couldn't process legacy order: ${event.order.id} due to : $e" }
            throw LegacyOrderProcessingException(e)
        }
    }

    private fun convertLegacyOrder(event: LegacyOrderSubmitted) =
        Order(
            id = OrderId(value = ObjectId().toHexString()),
            legacyOrderId = event.order.id,
            createdAt = event.order.dateCreated.toInstant(),
            updatedAt = event.order.dateUpdated.toInstant(),
            requestingUser = convertLegacyUser(event.requestingUser),
            authorisingUser = convertLegacyUser(event.authorisingUser),
            isbnOrProductNumber = event.order.extraFields.isbnOrProductNumber,
            status = OrderStatusConverter.from(event.order.status),
            items = event.orderItems.map { convertLegacyItem(it) },
            organisation = OrderOrganisation(name = event.authorisingUser.organisation.name),
            isThroughPlatform = true,
            currency = null,
            fxRateToGbp = null
        )

    private fun convertLegacyUser(user: LegacyOrderUser) =
        OrderUser.CompleteUser(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            legacyUserId = user.id
        )

    fun convertLegacyItem(item: LegacyOrderItem) =
        OrderItem(
            id = item.uuid,
            price = Price(amount = null, currency = null),
            transcriptRequested = item.transcriptsRequired,
            trim = item.trimming.parseTrimRequest(),
            video = videoProvider.get(VideoId(value = item.assetId)),
            license = null,
            notes = null
        )
}
