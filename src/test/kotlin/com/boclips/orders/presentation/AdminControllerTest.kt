package com.boclips.orders.presentation

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import testsupport.AbstractSpringIntegrationTest
import testsupport.OrderFactory
import testsupport.asOperator
import testsupport.asTeacher

class AdminControllerTest : AbstractSpringIntegrationTest() {
    private val broadcastOrdersUrl =
        "/v1/admin/orders/actions/broadcast_orders"

    @BeforeEach
    fun setUp() {
        saveOrder()
    }

    @Test
    fun `broadcast order events`() {
        mockMvc.perform(
            post(broadcastOrdersUrl).asOperator()
        ).andExpect(status().isOk)
    }

    @Test
    fun `broadcast order events returns 403 when user is not allowed`() {
        mockMvc.perform(
            post(broadcastOrdersUrl).asTeacher()
        ).andExpect(status().isForbidden)
    }
}