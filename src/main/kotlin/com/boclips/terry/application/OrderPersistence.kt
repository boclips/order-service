package com.boclips.terry.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.LegacyOrderItem
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.terry.application.exceptions.LegacyOrderProcessingException
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.model.orderItem.ContentPartner
import com.boclips.terry.domain.model.orderItem.ContentPartnerId
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.domain.model.orderItem.Video
import com.boclips.terry.domain.model.orderItem.VideoId
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.service.client.VideoServiceClient
import mu.KLogging
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class OrderPersistence(
    private val repo: OrdersRepository,
    private val legacyOrdersRepository: LegacyOrdersRepository,
    private val videoServiceClient: VideoServiceClient
) {
    companion object : KLogging()

    @BoclipsEventListener
    fun onLegacyOrderSubmitted(event: LegacyOrderSubmitted) {
        try {
            repo.add(
                order = Order(
                    id = OrderId(event.order.id),
                    uuid = event.order.uuid,
                    createdAt = event.order.dateCreated.toInstant(),
                    updatedAt = event.order.dateUpdated.toInstant(),
                    creatorEmail = event.creator,
                    vendorEmail = event.vendor,
                    isbnOrProductNumber = event.order.extraFields.isbnOrProductNumber,
                    status = OrderStatus.parse(event.order.status),
                    items = event.orderItems.map { createOrderItem(it) }
                )
            )

            legacyOrdersRepository.add(
                LegacyOrderDocument(
                    order = event.order,
                    items = event.orderItems,
                    creator = event.creator,
                    vendor = event.vendor
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

    fun createOrderItem(item: LegacyOrderItem): OrderItem {
        val videoResource = videoServiceClient.rawIdToVideoId(item.id).let { videoServiceClient.get(it) }

        return OrderItem(
            uuid = item.uuid,
            price = item.price,
            transcriptRequested = item.transcriptsRequired,
            contentPartner = ContentPartner(
                referenceId = ContentPartnerId(value = videoResource.contentPartnerId),
                name = videoResource.createdBy
            ),
            video = Video(
                referenceId = VideoId(value = videoResource.videoId.value),
                title = videoResource.title,
                type = videoResource.type.toString()
            )
        )
    }
}
