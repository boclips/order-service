package com.boclips.videos.service.testsupport

import com.boclips.terry.infrastructure.orders.FakeOrdersRepository
import com.boclips.videos.service.client.internal.FakeClient
import com.boclips.videos.service.client.spring.MockVideoServiceClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fake-security")
@MockVideoServiceClient
abstract class AbstractSpringIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var fakeOrdersRepository: FakeOrdersRepository

    @Autowired
    lateinit var fakeVideoClient: FakeClient

    @BeforeEach
    fun setup() {
        fakeOrdersRepository.clear()
    }
}
