package com.boclips.orders.infrastructure.users

import com.boclips.users.api.factories.OrganisationResourceFactory
import com.boclips.users.api.factories.UserResourceFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import testsupport.AbstractSpringIntegrationTest

internal class ApiUsersClientIntegrationTest : AbstractSpringIntegrationTest() {

    @Autowired
    lateinit var apiUsersClient: ApiUsersClient

    @Test
    fun `get orderUser by id`() {
        usersClient.add(
            UserResourceFactory.sample(
                id = "user-id",
                email = "order-user@email.com",
                firstName = "test",
                lastName = "last-name",
                organisation = OrganisationResourceFactory.sampleDetails(
                    id = "organisation-id",
                    name = "fancy org"
                )
            )
        )

        val orderUser = apiUsersClient.getUser("user-id")

        Assertions.assertThat(orderUser.userId).isEqualTo("user-id")
        Assertions.assertThat(orderUser.email).isEqualTo("order-user@email.com")
        Assertions.assertThat(orderUser.organisationId).isEqualTo("organisation-id")
        Assertions.assertThat(orderUser.organisationName).isEqualTo("fancy org")
        Assertions.assertThat(orderUser.firstName).isEqualTo("test")
        Assertions.assertThat(orderUser.lastName).isEqualTo("last-name")
    }
}
