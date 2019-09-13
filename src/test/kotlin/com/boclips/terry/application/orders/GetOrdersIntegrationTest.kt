package com.boclips.terry.application.orders

import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.TestFactories

class GetOrdersIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var getOrders: GetOrders

    @Test
    fun `can get a list of order resources`() {
        ordersRepository.add(TestFactories.order(legacyOrderId = "hello"))
        ordersRepository.add(TestFactories.order(legacyOrderId = "bye"))

        val retrievedOrders = getOrders()

        assertThat(retrievedOrders.map { it.legacyOrderId })
            .containsExactlyInAnyOrder("hello", "bye")
    }
}
