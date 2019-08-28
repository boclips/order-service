package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.OrderItem
import com.boclips.terry.infrastructure.orders.OrderDocument
import com.boclips.videos.service.client.VideoType
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant

class OrderDocumentConverterTest {
    @Test
    fun `converts an order to document to order`() {
        val originalOrder = TestFactories.order(
            id = OrderId(value = "5c1786db5236de0001d77747"),
            orderProviderId = "order-provider",
            authorisingUser = TestFactories.orderUser(
                email = "test@test.test"
            ),
            requestingUser = TestFactories.orderUser(
                email = "hello@hello.hello"
            ),
            status = OrderStatus.COMPLETED,
            createdAt = Instant.ofEpochSecond(100),
            updatedAt = Instant.ofEpochSecond(100),
            items = listOf(
                OrderItem(
                    uuid = "123",
                    price = BigDecimal.TEN,
                    transcriptRequested = false,
                    contentPartner = TestFactories.contentPartner(
                        name = "Bob was here"
                    ),
                    video = TestFactories.video(
                        title = "a video",
                        videoType = VideoType.NEWS
                    )
                )
            )
        )

        val document = OrderDocumentConverter.toOrderDocument(originalOrder)
        val reconvertedOrder = OrderDocumentConverter.toOrder(document)

        assertThat(reconvertedOrder).isEqualTo(originalOrder)
    }

    @Test
    fun `defaults to empty list if items are missing`() {
        val id = ObjectId()
        assertThat(
            OrderDocument(
                id = id,
                orderProviderId = "1234",
                status = "COMPLETED",
                authorisingUser = TestFactories.orderUserDocument(),
                requestingUser = TestFactories.orderUserDocument(),
                updatedAt = Instant.MAX,
                createdAt = Instant.EPOCH,
                isbnOrProductNumber = "anisbn",
                items = null
            ).let(OrderDocumentConverter::toOrder).items
        ).isEmpty()
    }
}
