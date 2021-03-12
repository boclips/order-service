package com.boclips.orders.presentation

import com.boclips.eventbus.events.order.OrderCreated
import com.boclips.eventbus.events.order.OrderSource as EventBusOrderSource
import com.boclips.orders.application.orders.EmailOrderConfirmation
import com.boclips.orders.domain.exceptions.OrderNotFoundException
import com.boclips.orders.domain.model.OrderSource
import com.boclips.orders.domain.service.events.EventConverter
import com.boclips.users.api.factories.UserResourceFactory
import com.boclips.users.api.response.organisation.OrganisationDetailsResource
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import testsupport.AbstractSpringIntegrationTest
import testsupport.CartFactory
import testsupport.OrderFactory
import testsupport.asPublisher

class OrdersConfirmationEmailIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var emailOrderConfirmation: EmailOrderConfirmation

    @Test
    fun `order is placed`() {
        defaultVideoClientResponse()
        usersClient.add(
            UserResourceFactory.sample(
                id = "user-id",
                organisation = OrganisationDetailsResource(
                    id = "org-id",
                    name = "an org name",
                    allowsOverridingUserIds = null,
                    country = null,
                    domain = null,
                    features = null,
                    state = null,
                    type = null
                )
            )
        )
        mongoCartsRepository.create(CartFactory.sample(userId = "user-id", items = listOf(CartFactory.cartItem()), note = "hello"))

        val orderLocationUrl = mockMvc.perform(
            (
                MockMvcRequestBuilders.post("/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                                {
                                   "note":"hello",
                                   "items":[
                                      {
                                         "id":"item-id",
                                         "videoId":"video-service-id",
                                         "additionalServices": {
                                            "trim": {
                                                "from":"1:00",
                                                "to":"2:00"
                                            },
                                            "transcriptRequested": true,
                                            "captionsRequested": true,
                                            "editRequest": "please remove images of alcohol"
                                         }
                                      }
                                   ],
                                   "user": {
                                      "id":"user-id",
                                      "email":"definitely-not-batman@wayne.com",
                                      "firstName":"Bruce",
                                      "lastName":"Wayne",
                                      "organisation": {
                                         "id":"org-id",
                                         "name":"Wayne Enterprises"
                                      }
                                   }
                                }
                        """.trimIndent()
                    ).asPublisher(userId = "user-id")
                )
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
            .andExpect(MockMvcResultMatchers.header().exists("Location"))
            .andReturn().response.getHeader("Location")!!

        mockMvc.perform(MockMvcRequestBuilders.get(orderLocationUrl).asPublisher())

        val events = eventBus.getEventsOfType(OrderCreated::class.java)
        Assertions.assertThat(events).hasSize(1)
        Assertions.assertThat(events[0].order.orderSource).isEqualTo(EventBusOrderSource.BOCLIPS)

        verify(emailSender, times(1)).sendOrderConfirmation(any())
    }

    @ParameterizedTest
    @EnumSource(value = OrderSource::class, names = ["MANUAL", "LEGACY"])
    fun `does not send confirmation for orders with source other than BOCLIPS`(orderSource: OrderSource) {
        val order = OrderFactory.order(orderSource = orderSource)
        saveOrder(order)

        emailOrderConfirmation.onOrderPlaced(OrderCreated(EventConverter().convertOrder(order)))

        verify(emailSender, never()).sendOrderConfirmation(any())
    }

    @Test
    fun `throws exception when order is not found`() {
        assertThrows<OrderNotFoundException> {
            emailOrderConfirmation.onOrderPlaced(OrderCreated(EventConverter().convertOrder(OrderFactory.order())))
        }
    }
}
