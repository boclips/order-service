package com.boclips.terry.presentation

import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import com.boclips.videos.service.testsupport.AbstractSpringIntegrationTest
import org.bson.types.ObjectId
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.TestFactories
import testsupport.asBackofficeStaff

class OrdersControllerIntegrationTest : AbstractSpringIntegrationTest() {
    @Autowired
    lateinit var ordersRepository: FakeOrdersRepository

    @BeforeEach
    fun setup() {
        ordersRepository.clear()
    }

    @Test
    fun `user without permission to view orders is forbidden from listing orders`() {
        mockMvc.perform(
            get("/v1/orders")
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `can get orders`() {
        val id = ObjectId().toHexString()
        val legacyOrder = TestFactories.legacyOrder(id)

        ordersRepository.add(TestFactories.order(legacyOrder), TestFactories.legacyOrderDocument(legacyOrder))

        mockMvc.perform(
            get("/v1/orders").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.orders[0].id", equalTo(id)))
            .andExpect(jsonPath("$._links.self").exists())
    }

    @Test
    fun `empty orders propagates in json response`() {
        mockMvc.perform(
            get("/v1/orders").asBackofficeStaff()
        ).andExpect(status().isOk)
            .andExpect(jsonPath("$._embedded.orders").exists())
    }
}
