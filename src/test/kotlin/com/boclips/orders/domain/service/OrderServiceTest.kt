package com.boclips.orders.domain.service

import com.boclips.orders.application.orders.IllegalOrderStateExport
import com.boclips.orders.domain.model.OrderStatus
import com.boclips.orders.domain.model.OrderUpdateCommand
import com.boclips.orders.domain.model.Price
import com.boclips.orders.domain.model.orderItem.AssetStatus
import com.boclips.orders.domain.model.orderItem.Duration
import com.boclips.orders.domain.model.orderItem.OrderItemLicense
import com.boclips.videos.api.request.VideoServiceApiFactory
import com.boclips.videos.api.response.video.CaptionStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest
import testsupport.BigDecimalWith2DP
import testsupport.OrderFactory
import testsupport.PriceFactory
import testsupport.TestFactories
import java.math.BigDecimal
import java.util.Currency

class OrderServiceTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var orderService: OrderService

    @Test
    fun `can create an order`() {
        defaultVideoClientResponse()
        val originalOrder = OrderFactory.completeOrder()

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)

        assertThat(originalOrder).isEqualTo(retrievedOrder)
    }

    @Test
    fun `a created order is ready if it has a currency and all items are ready`() {
        defaultVideoClientResponse(videoId = "123")
        val originalOrder = OrderFactory.order(
            status = OrderStatus.INCOMPLETED,
            items = listOf(
                OrderFactory.orderItem(video = TestFactories.video(videoServiceId = "123"))
            )
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.READY)
    }

    @Test
    fun `a created order is incomplete if a single item is in complete`() {
        val originalOrder = OrderFactory.order(
            status = OrderStatus.IN_PROGRESS,
            items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(downloadableVideoStatus = AssetStatus.UNAVAILABLE)
                )
            )
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `a created order is in progress if a single item is in progress`() {
        val originalOrder = OrderFactory.order(
            status = OrderStatus.INCOMPLETED,
            items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(captionStatus = AssetStatus.PROCESSING)
                )
            )
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.IN_PROGRESS)
    }

    @Test
    fun `a created order requests captions when 'transcriptRequested' and the caption status is updated`() {
        val video1 = fakeVideoClient.createVideo(VideoServiceApiFactory.createCreateVideoRequest())
        val video2 = fakeVideoClient.createVideo(VideoServiceApiFactory.createCreateVideoRequest())
        val originalOrder = OrderFactory.order(
            status = OrderStatus.INCOMPLETED,
            items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(videoServiceId = video1.id!!, captionStatus = AssetStatus.UNAVAILABLE),
                    price = PriceFactory.tenDollars(),
                    license = OrderItemLicense(duration = Duration.Description("5 years"), territory = "UK"),
                    transcriptRequested = true
                ),
                OrderFactory.orderItem(
                    video = TestFactories.video(videoServiceId = video2.id!!, captionStatus = AssetStatus.UNAVAILABLE),
                    price = PriceFactory.tenDollars(),
                    license = OrderItemLicense(duration = Duration.Description("5 years"), territory = "UK"),
                    transcriptRequested = false
                )
            )
        )

        val createdOrder = orderService.createIfNonExistent(originalOrder)

        assertThat(fakeVideoClient.getVideo(video1.id!!).captionStatus)
            .isEqualTo(CaptionStatus.REQUESTED)
        assertThat(fakeVideoClient.getVideo(video2.id!!).captionStatus)
            .isEqualTo(CaptionStatus.NOT_AVAILABLE)

        assertThat(createdOrder.items[0].video.captionStatus).isEqualTo(AssetStatus.REQUESTED)
        assertThat(createdOrder.items[1].video.captionStatus).isEqualTo(AssetStatus.UNAVAILABLE)
    }

    @Test
    fun `captions aren't requested if already available`() {
        // Right now this is the only way to set captions as already requested
        val videoResource = fakeVideoClient.createVideo(VideoServiceApiFactory.createCreateVideoRequest())
            .also { fakeVideoClient.updateCaptionStatus(it.id!!, CaptionStatus.HUMAN_GENERATED_AVAILABLE) }

        val originalOrder = OrderFactory.order(
            status = OrderStatus.INCOMPLETED,
            items = listOf(
                OrderFactory.orderItem(
                    video = TestFactories.video(videoServiceId = videoResource.id!!),
                    price = PriceFactory.tenDollars(),
                    license = OrderItemLicense(duration = Duration.Description("5 years"), territory = "UK")
                )
            )
        )
        val createdOrder = orderService.createIfNonExistent(originalOrder)

        assertThat(createdOrder.items[0].video.captionStatus).isEqualTo(AssetStatus.AVAILABLE)
    }

    @Test
    fun `an order with missing item license is not complete`() {
        val originalOrder = OrderFactory.order(
            status = OrderStatus.INCOMPLETED,
            items = listOf(OrderFactory.orderItem(price = PriceFactory.tenDollars(), license = null))
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `cannot replace status of an order to complete if it's not completed`() {
        val order = OrderFactory.order(
            status = OrderStatus.READY,
            items = listOf(OrderFactory.orderItem(price = Price(amount = null, currency = null)))
        )

        orderService.createIfNonExistent(order)

        val retreivedOrder = ordersRepository.findOne(order.id)!!

        assertThat(retreivedOrder.status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `a cancelled order can not been complete`() {
        val originalOrder = OrderFactory.order(
            status = OrderStatus.CANCELLED,
            items = listOf(OrderFactory.orderItem(price = PriceFactory.tenDollars()))
        )

        orderService.createIfNonExistent(originalOrder)

        val retrievedOrder = ordersRepository.findOne(originalOrder.id)!!

        assertThat(retrievedOrder.status).isEqualTo(OrderStatus.CANCELLED)
    }

    @Test
    fun `ignores orders with a clashing legacy id`() {
        ordersRepository.save(OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.INCOMPLETED))

        val newOrder = OrderFactory.order(legacyOrderId = "hi", status = OrderStatus.CANCELLED)

        orderService.createIfNonExistent(newOrder)

        val retrievedOrders = ordersRepository.findAll()

        assertThat(retrievedOrders).hasSize(1)
        assertThat(retrievedOrders.first().status).isEqualTo(OrderStatus.INCOMPLETED)
    }

    @Test
    fun `when any order has status invalid, throws`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.INVALID,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        assertThrows<IllegalOrderStateExport> {
            orderService.exportManifest(emptyMap())
        }
    }

    @Test
    fun `export both ready and incompleted orders`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.INCOMPLETED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        val manifest = orderService.exportManifest(
            mapOf(
                Currency.getInstance("USD") to BigDecimal.TEN,
                Currency.getInstance("GBP") to BigDecimal.ONE
            )
        )

        assertThat(manifest.items).hasSize(3)
    }

    @Test
    fun `when any orders are cancelled they are filtered`() {
        listOf(
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.CANCELLED,
                items = listOf(OrderFactory.orderItem())
            ),
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(OrderFactory.orderItem())
            )
        ).forEach { ordersRepository.save(it) }

        val manifest = orderService.exportManifest(
            mapOf(
                Currency.getInstance("USD") to BigDecimal.TEN,
                Currency.getInstance("GBP") to BigDecimal.ONE
            )
        )
        assertThat(manifest.items).hasSize(2)
    }

    @Test
    fun `exports manifest with correct fx rates`() {
        val order =
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(
                    OrderFactory.orderItem(
                        price = PriceFactory.tenDollars(),
                        video = TestFactories.video(
                            channel = TestFactories.channel(
                                currency = Currency.getInstance(
                                    "SGD"
                                )
                            )
                        )
                    )

                )
            )

        orderService.createIfNonExistent(order)

        val manifest = orderService.exportManifest(
            fxRatesAgainstPound = mapOf(
                Currency.getInstance("USD") to BigDecimal.valueOf(4),
                Currency.getInstance("SGD") to BigDecimal.valueOf(2)
            )
        )

        assertThat(manifest.items).hasSize(1)
        assertThat(manifest.items.first().fxRate).isEqualTo(BigDecimal.valueOf(0.50000).setScale(5))
        assertThat(manifest.items.first().convertedSalesAmount.amount).isEqualTo(BigDecimalWith2DP.valueOf(5))
        assertThat(manifest.items.first().convertedSalesAmount.currency).isEqualTo(Currency.getInstance("SGD"))
    }

    @Test
    fun `export manifest has empty cells where values are missing`() {
        val order =
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(
                    OrderFactory.orderItem(
                        license = null,
                        price = PriceFactory.empty(),
                        video = TestFactories.video(
                            channel = TestFactories.channel(
                                currency = Currency.getInstance(
                                    "SGD"
                                )
                            )
                        )
                    )

                )
            )
        orderService.createIfNonExistent(order)

        val manifest = orderService.exportManifest(
            fxRatesAgainstPound = emptyMap()
        )

        assertThat(manifest.items).hasSize(1)
        assertThat(manifest.items.first().salePrice.currency).isNull()
        assertThat(manifest.items.first().salePrice.amount).isNull()
        assertThat(manifest.items.first().license).isNull()
    }

    @Test
    fun `when pricing and currency is provided but no fx rate null is returned`() {
        val order =
            OrderFactory.order(
                status = OrderStatus.READY,
                items = listOf(
                    OrderFactory.orderItem(
                        license = null,
                        price = PriceFactory.onePound(),
                        video = TestFactories.video(
                            channel = TestFactories.channel(
                                currency = Currency.getInstance(
                                    "SGD"
                                )
                            )
                        )
                    )

                )
            )
        orderService.createIfNonExistent(order)

        val manifest = orderService.exportManifest(
            fxRatesAgainstPound = emptyMap()
        )

        assertThat(manifest.items).hasSize(1)
        assertThat(manifest.items.first().fxRate).isNull()
        assertThat(manifest.items.first().salePrice.amount).isEqualTo(BigDecimalWith2DP.valueOf(1))
        assertThat(manifest.items.first().salePrice.currency).isEqualTo(Currency.getInstance("GBP"))
        assertThat(manifest.items.first().license).isNull()
    }

    @Test
    fun `updating an order also updates its status`() {
        defaultVideoClientResponse("123")
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(
                    price = Price(
                        amount = null,
                        currency = Currency.getInstance("USD")
                    ),
                    video = TestFactories.video("123")
                )
            ),
            status = OrderStatus.INCOMPLETED
        )

        orderService.createIfNonExistent(order)

        val updatedOrder = orderService.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice(
                order.id,
                order.items.first().id,
                BigDecimal.valueOf(100)
            )
        )

        assertThat(updatedOrder.status).isEqualTo(OrderStatus.READY)
    }

    @Test
    fun `can bulk update an order`() {
        val order =
            OrderFactory.order(items = listOf(OrderFactory.orderItem(id = "1", price = PriceFactory.zeroEuros())))

        orderService.createIfNonExistent(order)

        orderService.bulkUpdate(
            listOf(
                OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice(
                    orderId = order.id,
                    orderItemsId = "1",
                    amount = BigDecimal.ONE
                ),
                OrderUpdateCommand.UpdateOrderCurrency(
                    orderId = order.id,
                    currency = Currency.getInstance("USD"),
                    fxRateToGbp = BigDecimal("1.5")
                )
            )
        )

        val updatedOrder = ordersRepository.findOne(order.id)!!
        assertThat(updatedOrder.currency).isEqualTo(Currency.getInstance("USD"))
        assertThat(updatedOrder.fxRateToGbp).isEqualTo("1.5")
        assertThat(updatedOrder.items[0].price.amount).isEqualTo(BigDecimalWith2DP.ONE)
    }

    @Test
    fun `Orders are converted to their CP's currency`() {
        defaultVideoClientResponse(videoId = "123")
        val order = OrderFactory.order(
            items = listOf(
                OrderFactory.orderItem(
                    price = Price(
                        amount = BigDecimal.valueOf(100),
                        currency = Currency.getInstance("USD")
                    ),
                    video = TestFactories.video(videoServiceId = "123")
                )
            ),
            status = OrderStatus.INCOMPLETED
        )

        orderService.createIfNonExistent(order)

        val updatedOrder = orderService.update(
            OrderUpdateCommand.OrderItemUpdateCommand.UpdateOrderItemPrice(
                order.id,
                order.items.first().id,
                BigDecimal.valueOf(100)
            )
        )

        assertThat(updatedOrder.status).isEqualTo(OrderStatus.READY)
    }
}
