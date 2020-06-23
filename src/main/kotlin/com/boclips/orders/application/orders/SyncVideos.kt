package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.infrastructure.VideoServiceVideoProvider
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class SyncVideos(
    private val ordersRepository: OrdersRepository,
    private val videoServiceVideoProvider: VideoServiceVideoProvider
) {
    companion object : KLogging()

    operator fun invoke() {
        ordersRepository.streamAll {
            it.windowed(100, 100, true).forEachIndexed { index, orders ->
                logger.info { "Starting batch ${index + 1} of syncing videos" }
                val commands = orders.flatMap { order ->
                    order.items.mapNotNull { item ->
                        try {
                            val newVideo = videoServiceVideoProvider.get(item.video.videoServiceId)
                            OrderUpdateCommand.OrderItemUpdateCommand.ReplaceVideo(orderId = order.id, orderItemsId = item.id, video = newVideo)
                        } catch (e: Exception) {
                            logger.warn("Error syncing video for order: ${order.id.value}", e)
                            null
                        }
                    }
                }

                ordersRepository.bulkUpdate(commands)
            }
        }
    }
}
