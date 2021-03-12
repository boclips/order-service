package com.boclips.orders.config

import com.boclips.orders.config.properties.UserClientProperties
import com.boclips.users.api.httpclient.UsersClient
import com.boclips.users.api.httpclient.helper.ServiceAccountCredentials
import com.boclips.users.api.httpclient.helper.ServiceAccountTokenFactory
import feign.okhttp.OkHttpClient
import feign.opentracing.TracingClient
import io.opentracing.Tracer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!test")
@Configuration
class UsersClientConfig {
    @Bean
    fun usersClient(
        userClientProperties: UserClientProperties,
        tracer: Tracer
    ): UsersClient = UsersClient.create(
        apiUrl = userClientProperties.baseUrl,
        feignClient = createTracingClient(tracer),
        tokenFactory = userClientProperties.tokenFactory()
    )
    private fun createTracingClient(tracer: Tracer): TracingClient {
        val delegate = OkHttpClient()
        return TracingClient(delegate, tracer)
    }
}
fun UserClientProperties.tokenFactory() =
    ServiceAccountTokenFactory(
        serviceAccountCredentials = ServiceAccountCredentials(
            authEndpoint = tokenUrl,
            clientId = clientId,
            clientSecret = clientSecret
        )
    )
