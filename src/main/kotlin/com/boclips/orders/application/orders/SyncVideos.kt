package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.OrderFilter
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.infrastructure.VideoServiceVideoProvider
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class SyncVideos(
    private val ordersRepository: OrdersRepository,
    private val videoServiceVideoProvider: VideoServiceVideoProvider,
    private val orderService: OrderService
) {
    companion object : KLogging()

    operator fun invoke() {
        ordersRepository.streamAll(filter = OrderFilter.HasStatus(OrderStatus.INCOMPLETED, OrderStatus.IN_PROGRESS)) {
            it.windowed(50, 50, true).forEachIndexed { index, orders ->
                logger.info { "Starting batch ${index + 1} of syncing videos" }

                val commands = orders.flatMap { order ->
                    logger.info { "Processing ${order.items.size} items for order: ${order.id.value}" }

                    return@flatMap order.items.mapNotNull { item ->
                        try {
                            val newVideo = videoServiceVideoProvider.get(item.video.videoServiceId)
                            OrderUpdateCommand.OrderItemUpdateCommand.ReplaceVideo(
                                orderId = order.id,
                                orderItemsId = item.id,
                                video = newVideo
                            )
                        } catch (e: Exception) {
                            logger.warn("Error syncing video for order: ${order.id.value}", e)
                            null
                        }
                    }
                }

                orderService.bulkUpdate(commands)
            }
        }
    }
}
