package com.boclips.terry.presentation

import com.boclips.terry.application.GetOrder
import com.boclips.terry.application.GetOrders
import com.boclips.terry.presentation.hateos.HateoasEmptyCollection
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resource
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/orders")
class OrdersController(
    private val getOrders: GetOrders,
    private val getOrder: GetOrder
) {
    companion object {
        fun getOrdersLink(): Link = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
        ).withRel("orders")

        fun getSelfOrdersLink(): Link =
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
            ).withRel("orders").withSelfRel()

        fun getSelfOrderLink(id: String): Link = ControllerLinkBuilder.linkTo(
            ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderResource(id)
        ).withSelfRel()
    }

    @GetMapping
    fun getOrderList(): Resources<*> {
        val orders = getOrders()

        val orderResources = orders
            .map { wrapOrder(it) }
            .let(HateoasEmptyCollection::fixIfEmptyCollection)

        return Resources(orderResources, getSelfOrdersLink())
    }

    @RequestMapping("/{id}")
    @GetMapping
    fun getOrderResource(@PathVariable("id") id: String): Resource<OrderResource> {
        val order = getOrder(id)

        return wrapOrder(order)
    }

    private fun wrapOrder(orderResource: OrderResource): Resource<OrderResource> {
        return Resource(orderResource, getSelfOrderLink(orderResource.id))
    }
}
