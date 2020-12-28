package com.boclips.orders.presentation.hateos

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.core.EmbeddedWrappers

object HateoasEmptyCollection {
    inline fun <reified T> fixIfEmptyCollection(resources: List<EntityModel<T>>): List<*> {
        if (resources.isNotEmpty()) {
            return resources
        }
        return listOf(EmbeddedWrappers(false).emptyCollectionOf(T::class.java))
    }
}
