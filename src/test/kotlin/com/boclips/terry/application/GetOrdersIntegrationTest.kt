package com.boclips.terry.application

import com.boclips.terry.domain.OrderItem
import com.boclips.terry.domain.OrderStatus
import com.boclips.terry.presentation.resources.OrderResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories
import java.math.BigDecimal
import java.time.Instant

class GetOrdersIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getOrders: GetOrders

    @Test
    fun `can get a list of order resources`() {
        val legacyOrder = TestFactories.legacyOrder(ObjectId().toHexString())
        val now = Instant.EPOCH
        val order1 = TestFactories.order(
            legacyOrder = legacyOrder,
            creatorEmail = "boclips@example.com",
            vendorEmail = "big-bang@example.com",
            status = OrderStatus.CONFIRMED,
            createdAt = now.plusMillis(1),
            updatedAt = now.plusMillis(2),
            items = listOf(
                OrderItem(
                    uuid = "i-love-uuids",
                    price = BigDecimal.ONE,
                    transcriptRequested = true
                )
            )
        )
        fakeOrdersRepository.add(
            order = order1,
            legacyDocument = TestFactories.legacyOrderDocument(
                legacyOrder,
                "creator@theworld.example",
                "some@vendor.4u",
                listOf(
                    TestFactories.legacyOrderItem(
                        uuid = "i-love-uuids",
                        price = BigDecimal.ONE,
                        transcriptsRequired = true
                    )
                )
            )
        )

        val legacyOrder2 = TestFactories.legacyOrder(ObjectId().toHexString())
        val order2 = TestFactories.order(
            legacyOrder = legacyOrder,
            creatorEmail = "boclips@example.com",
            vendorEmail = "big-bang@example.com",
            status = OrderStatus.CONFIRMED,
            createdAt = now.plusMillis(3),
            updatedAt = now.plusMillis(4),
            items = listOf(
                OrderItem(
                    uuid = "i-also-lurve-uuids",
                    price = BigDecimal.ONE,
                    transcriptRequested = true
                )
            )
        )
        fakeOrdersRepository.add(
            order = order2,
            legacyDocument = TestFactories.legacyOrderDocument(
                legacyOrder2,
                "creator@theworld.example",
                "some@vendor.4u",
                listOf(
                    TestFactories.legacyOrderItem(
                        uuid = "item1-uuid",
                        transcriptsRequired = true,
                        price = BigDecimal.ONE
                    )
                )
            )
        )

        assertThat(getOrders())
            .isEqualTo(
                listOf(order1, order2)
                    .map(OrderResource.Companion::fromOrder)
            )
    }
}
