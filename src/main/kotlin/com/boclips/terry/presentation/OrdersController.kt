package com.boclips.terry.presentation

import com.boclips.terry.application.GetOrders
import com.boclips.terry.presentation.resources.OrderResource
import org.springframework.hateoas.Link
import org.springframework.hateoas.Resources
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/orders")
class OrdersController(
    private val getOrders: GetOrders
) {
    companion object {
        fun getOrdersLink(): Link =
            ControllerLinkBuilder.linkTo(
                ControllerLinkBuilder.methodOn(OrdersController::class.java).getOrderList()
            ).withSelfRel()
    }

    @GetMapping
    fun getOrderList(): Resources<OrderResource> =
        Resources(getOrders(), getOrdersLink())
}
