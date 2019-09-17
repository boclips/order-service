package com.boclips.terry.application.orders

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.eventbus.events.order.LegacyOrderUser
import com.boclips.terry.application.exceptions.LegacyOrderProcessingException
import com.boclips.terry.application.orders.converters.LicenseConverter
import com.boclips.terry.application.orders.converters.OrderStatusConverter
import com.boclips.terry.application.orders.converters.TrimmingConverter
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderOrganisation
import com.boclips.terry.domain.model.OrderUpdateCommand
import com.boclips.terry.domain.model.OrderUser
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.model.Price
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.domain.service.VideoProvider
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.service.client.VideoServiceClient
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class StoreLegacyOrder(
    private val repo: OrdersRepository,
    private val legacyOrdersRepository: LegacyOrdersRepository,
    private val videoServiceClient: VideoServiceClient,
    private val videoProvider: VideoProvider
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {
        try {
            val order = convertLegacyOrder(event)

            // Ideally and update and create would be two different events so we wouldn't have to distinguish them here
            val foundOrder = repo.findOneByLegacyId(order.legacyOrderId)
            if (foundOrder != null) {
                repo.update(
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
        } catch (e: IllegalStateException) {
            logger.error { "Couldn't process legacy order: $e" }
        } catch (e: Exception) {
            logger.error { "Couldn't process legacy order: $e" }
            if (e !is IllegalStateException) {
                throw LegacyOrderProcessingException(e)
            }
        }
    }

    private fun convertLegacyOrder(event: LegacyOrderSubmitted): Order {
        return Order(
            id = OrderId(value = ObjectId().toHexString()),
            legacyOrderId = event.order.id,
            createdAt = event.order.dateCreated.toInstant(),
            updatedAt = event.order.dateUpdated.toInstant(),
            requestingUser = convertLegacyUser(event.requestingUser),
            authorisingUser = convertLegacyUser(event.authorisingUser),
            isbnOrProductNumber = event.order.extraFields.isbnOrProductNumber,
            status = OrderStatusConverter.from(event.order.status),
            items = event.orderItems.map { convertLegacyItem(it) },
            organisation = OrderOrganisation(name = event.authorisingUser.organisation.name)
        )
    }

    private fun convertLegacyUser(user: LegacyOrderUser): OrderUser {
        return OrderUser.CompleteUser(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            legacyUserId = user.id
        )
    }

    fun convertLegacyItem(item: LegacyOrderItem): OrderItem {
        return videoProvider.get(VideoId(value = item.assetId))?.let {
            OrderItem(
                price = Price(amount = item.price, currency = null),
                transcriptRequested = item.transcriptsRequired,
                trim = TrimmingConverter.toTrimRequest(item.trimming),
                video = it,
                license = LicenseConverter.toOrderItemLicense(item.license),
                notes = null
            )
        } ?: throw IllegalStateException("Could not find video for: ${item.assetId}")
    }
}
