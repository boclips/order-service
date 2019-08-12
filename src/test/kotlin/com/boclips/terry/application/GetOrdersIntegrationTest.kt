package com.boclips.terry.application

import com.boclips.terry.domain.model.OrderId
import com.boclips.terry.domain.model.OrderItem
import com.boclips.terry.domain.model.OrderStatus
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
            id = OrderId(value = legacyOrder.id),
            creatorEmail = "boclips@example.com",
            vendorEmail = "big-bang@example.com",
            status = OrderStatus.CONFIRMED,
            createdAt = now.plusMillis(6),
            updatedAt = now.plusMillis(5),
            items = listOf(
                OrderItem(
                    uuid = "i-love-uuids",
                    price = BigDecimal.ONE,
                    transcriptRequested = true,
                    video = TestFactories.video()
                )
            )
        )
        fakeOrdersRepository.add(
            order = order1
        )

        val order2 = TestFactories.order(
            id = OrderId(value = legacyOrder.id),
            creatorEmail = "boclips@example.com",
            vendorEmail = "big-bang@example.com",
            status = OrderStatus.CONFIRMED,
            createdAt = now.plusMillis(3),
            updatedAt = now.plusMillis(4),
            items = listOf(
                OrderItem(
                    uuid = "i-also-lurve-uuids",
                    price = BigDecimal.ONE,
                    transcriptRequested = true,
                    video = TestFactories.video()
                )
            )
        )
        fakeOrdersRepository.add(
            order = order2
        )

        assertThat(getOrders())
            .isEqualTo(
                listOf(order1, order2)
                    .map(OrderResource.Companion::fromOrder)
            )
    }
}
