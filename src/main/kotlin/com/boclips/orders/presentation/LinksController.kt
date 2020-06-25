package com.boclips.orders.presentation

import com.boclips.orders.config.security.UserRoles
import com.boclips.security.utils.UserExtractor
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
        if (UserExtractor.getCurrentUser()?.hasRole(UserRoles.VIEW_ORDERS) == true) {
            EntityModel(
                "",
                OrdersController.getOrdersLink(),
                OrdersController.getOrderLink(),
                OrdersController.getExportOrdersLink()
            )
        } else {
            EntityModel(
                "",
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LinksController::class.java).getLinks())
                    .withSelfRel()
            )
        }
}
