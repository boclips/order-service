package com.boclips.orders.application.orders

import com.boclips.orders.domain.model.OrderFilter
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.OrdersRepository
import com.boclips.orders.domain.service.OrderService
import com.boclips.orders.infrastructure.VideoServiceVideoProvider
import mu.KLogging
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class SyncVideos(
    private val ordersRepository: OrdersRepository,
    private val videoServiceVideoProvider: VideoServiceVideoProvider,
    private val orderService: OrderService
) {
    companion object : KLogging()

    operator fun invoke() {
        val threadPool = Executors.newFixedThreadPool(15)
        ordersRepository.streamAll(filter = OrderFilter.HasStatus(OrderStatus.INCOMPLETED, OrderStatus.IN_PROGRESS)) {
            it.windowed(2, 2, true).forEachIndexed { index, orders ->
                logger.info { "Starting batch ${index + 1} of syncing videos" }

                orders.map { order ->
                    order.items.map { item ->
                        threadPool.execute {
                            try {
                                val newVideo = videoServiceVideoProvider.get(item.video.videoServiceId)
                                val updateCommand = OrderUpdateCommand.OrderItemUpdateCommand.ReplaceVideo(
                                    orderId = order.id,
                                    orderItemsId = item.id,
                                    video = newVideo
                                )
                                ordersRepository.update(updateCommand)
                            } catch (e: Exception) {
                                logger.warn("Error syncing video for order: ${order.id.value}", e)
                            }
                        }
                    }
                    orderService.syncStatus(order)
                }
            }
        }
        threadPool.shutdown()
        try {
            threadPool.awaitTermination(40, TimeUnit.MINUTES)
        } catch (e: Exception) {
            logger.error(e) { "something went wrong with syncVideos thread pool" }
        }
        logger.info { "shutting down" }
    }
}
