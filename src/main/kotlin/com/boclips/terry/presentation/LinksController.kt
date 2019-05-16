package com.boclips.terry.presentation

import com.boclips.security.utils.UserExtractor
import com.boclips.terry.config.security.UserRoles
import org.springframework.hateoas.Resource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1")
class LinksController {
    @GetMapping
    fun getLinks(): Resource<String> =
        if (UserExtractor.getCurrentUser()?.hasRole(UserRoles.VIEW_ORDERS) == true) {
            Resource("", listOf(OrdersController.getOrdersLink()))
        } else {
            Resource("")
        }
}
