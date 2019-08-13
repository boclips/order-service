package com.boclips.terry.infrastructure.orders.converters

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderStatus
import com.boclips.terry.domain.model.orderItem.OrderItem
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
            creatorEmail = "creator@s.com",
            vendorEmail = "vendor@m.com",
            status = OrderStatus.COMPLETED,
            createdAt = Instant.ofEpochSecond(100),
            updatedAt = Instant.ofEpochSecond(100),
            items = listOf(
                OrderItem(
                    uuid = "123",
                    price = BigDecimal.TEN,
                    transcriptRequested = false,
                    contentPartner = TestFactories.contentPartner(
                        id = ObjectId.get().toHexString(),
                        name = "Bob was here"
                    ),
                    video = TestFactories.video(
                        id = "456",
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
}
