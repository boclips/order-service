package com.boclips.orders.config.security

import com.boclips.security.EnableBoclipsSecurity
import com.boclips.security.HttpSecurityConfigurer
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.stereotype.Component

@Profile("!test")
@Configuration
@EnableBoclipsSecurity
class WebSecurityConfig

@Component
class OrderServiceHttpSecurityConfigurer : HttpSecurityConfigurer {
    override fun configure(http: HttpSecurity) {
        http
            .authorizeRequests()
            .antMatchers(HttpMethod.GET, "/").permitAll()
            .antMatchers(HttpMethod.GET, "/actuator/prometheus").permitAll()
            .antMatchers(HttpMethod.POST, "/slack").permitAll()
            .antMatchers(HttpMethod.POST, "/slack-interaction").permitAll()

            .antMatchers(HttpMethod.OPTIONS, "/v1/**").permitAll()

            .antMatchers("/v1").permitAll()
            .antMatchers("/v1/").permitAll()

            .antMatchers(HttpMethod.GET, "/v1/orders").hasRole(UserRoles.VIEW_ORDERS)
            .antMatchers(HttpMethod.GET, "/v1/orders/**").hasRole(UserRoles.VIEW_ORDERS)
            .antMatchers(HttpMethod.POST, "/v1/orders").hasAnyRole(UserRoles.CREATE_ORDERS, UserRoles.PLACE_ORDER)
            .antMatchers(HttpMethod.PATCH, "/v1/orders/**").hasRole(UserRoles.UPDATE_ORDERS)

            .antMatchers(HttpMethod.POST, "/v1/cart/items").hasRole(UserRoles.ADD_CART_ITEMS)
            .antMatchers(HttpMethod.DELETE, "/v1/cart/items/*").hasRole(UserRoles.DELETE_CART_ITEMS)
            .antMatchers(HttpMethod.PATCH, "/v1/cart/items/*/additional-services").hasRole(UserRoles.UPDATE_CART_ITEM)
            .antMatchers(HttpMethod.GET, "/v1/cart").hasRole(UserRoles.VIEW_CART)
            .antMatchers(HttpMethod.PATCH, "/v1/cart").hasRole(UserRoles.UPDATE_CART)

            .antMatchers(HttpMethod.POST, "/v1/admin/orders/actions/broadcast_orders").run {
                this.hasRole(UserRoles.BROADCAST_EVENTS)
            }

            .anyRequest().denyAll()
    }
}

