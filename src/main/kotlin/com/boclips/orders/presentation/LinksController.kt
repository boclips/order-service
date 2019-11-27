package com.boclips.orders.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.orders.config.security.UserRoles
import org.springframework.hateoas.Resource
import org.springframework.hateoas.mvc.ControllerLinkBuilder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController {
    @GetMapping
    fun getLinks(): Resource<String> =
        if (UserExtractor.getCurrentUser()?.hasRole(UserRoles.VIEW_ORDERS) == true) {
            Resource(
                "",
                OrdersController.getOrdersLink(),
                OrdersController.getOrderLink(),
                OrdersController.getExportOrdersLink()
            )
        } else {
            Resource(
                "",
                ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(LinksController::class.java).getLinks()).withSelfRel()
            )
        }
}
