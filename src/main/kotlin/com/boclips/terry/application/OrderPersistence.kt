package com.boclips.terry.application

import com.boclips.eventbus.BoclipsEventListener
import com.boclips.eventbus.events.order.LegacyOrderSubmitted
import com.boclips.terry.application.exceptions.LegacyOrderProcessingException
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.Order
import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.OrdersRepository
import com.boclips.terry.domain.model.Video
import com.boclips.terry.domain.model.VideoId
import com.boclips.terry.infrastructure.orders.LegacyOrderDocument
import com.boclips.videos.service.client.VideoServiceClient
import mu.KLogging
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
                    items = event.orderItems.map {
                        OrderItem(
                            uuid = it.uuid,
                            price = it.price,
                            transcriptRequested = it.transcriptsRequired,
                            video = getVideo(it.id)
                        )
                    }
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

    fun getVideo(videoId: String): Video = videoServiceClient.rawIdToVideoId(videoId).let {
        val videoResource = videoServiceClient.get(it)
        return Video(
            id = VideoId(videoResource.videoId.value),
            title = videoResource.title,
            source = videoResource.createdBy,
            type = videoResource.type.toString()
        )
    }
}
