package com.boclips.terry.application

import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import com.boclips.terry.presentation.OrderResource
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories

class GetOrdersIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var repo: FakeOrdersRepository

    @Autowired
    lateinit var getOrders: GetOrders

    @Test
    fun `can get a list of order resources`() {
        val id1 = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id1)
        val order1 = TestFactories.order(legacyOrder)
        repo.add(order1, TestFactories.legacyOrderDocument(legacyOrder))

        val id2 = ObjectId().toHexString()
        val legacyOrder2 = TestFactories.legacyOrder(id2)
        val order2 = TestFactories.order(legacyOrder)
        repo.add(order2, TestFactories.legacyOrderDocument(legacyOrder2))

        assertThat(getOrders())
            .isEqualTo(
                listOf(order1, order2)
                    .map(OrderResource.Companion::fromOrder)
            )
    }
}
