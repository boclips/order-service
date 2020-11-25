package com.boclips.orders.presentation

import com.boclips.orders.presentation.hateos.CartsLinkBuilder
import com.boclips.orders.presentation.hateos.OrdersLinkBuilder
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController {
    @GetMapping
    fun getLinks(): EntityModel<String> =

        EntityModel(
            "",
            listOfNotNull(
                OrdersLinkBuilder.getOrdersLink(),
                OrdersLinkBuilder.getOrderLink(),
                OrdersLinkBuilder.getExportOrdersLink(),
                CartsLinkBuilder.getCartLink(),
                getSelfLink()
            )
        )

    private fun getSelfLink() =
        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LinksController::class.java).getLinks())
            .withSelfRel()
}
