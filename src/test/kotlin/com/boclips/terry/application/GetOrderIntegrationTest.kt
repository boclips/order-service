package com.boclips.terry.application

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
import java.time.LocalDate
import java.time.ZoneOffset

class GetOrderIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var getOrder: GetOrder

    @Test
    fun `can get an order resource`() {
        val legacyOrder = TestFactories.legacyOrder(ObjectId().toHexString())
        val order = TestFactories.order(
            legacyOrder,
            creatorEmail = "boclips@example.com",
            vendorEmail = "big-bang@example.com",
            status = OrderStatus.CONFIRMED,
            createdAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            updatedAt = Instant.from(LocalDate.of(1995, 1, 1).atStartOfDay(ZoneOffset.UTC)),
            items = listOf(
                OrderItem(
                    uuid = "i-love-uuids",
                    price = BigDecimal.ONE,
                    transcriptRequested = true
                )
            )
        )
        fakeOrdersRepository.add(
            order = order,
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

        assertThat(getOrder(legacyOrder.id)).isEqualTo(OrderResource.fromOrder(order))
    }
}