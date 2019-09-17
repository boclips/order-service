package com.boclips.videos.service.testsupport

import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.infrastructure.orders.MongoOrdersRepository
import com.boclips.terry.infrastructure.orders.TestMongoProcess
import com.boclips.videos.service.client.Playback
import com.boclips.videos.service.client.Video
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.client.internal.FakeClient
import com.boclips.videos.service.client.spring.MockVideoServiceClient
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import de.flapdoodle.embed.mongo.MongodProcess
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.net.URI
import java.time.Duration
import java.time.LocalDate

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fake-security")
@MockVideoServiceClient
abstract class AbstractSpringIntegrationTest {
    companion object Setup {
        var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                mongoProcess =
                    TestMongoProcess.process
            }
        }
    }
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var ordersRepository: MongoOrdersRepository

    @Autowired
    lateinit var fakeVideoClient: FakeClient

    @Autowired
    lateinit var legacyOrdersRepository: LegacyOrdersRepository

    @BeforeEach
    fun setup() {
        ordersRepository.deleteAll()
        legacyOrdersRepository.clear()
    }

    @AfterEach
    fun tearDown() {
        Mockito.reset(fakeVideoClient)
    }

    fun defaultVideoClientResponse() {
        val video = Video.builder()
            .contentPartnerId("content-partner-id")
            .contentPartnerVideoId("video-id")
            .createdBy("our-content-partner")
            .description("video description")
            .playback(
                Playback.builder()
                    .duration(Duration.ofSeconds(10))
                    .playbackId("playback-id")
                    .thumbnailUrl("thumbnail/url")
                    .build()
            )
            .releasedOn(LocalDate.now())
            .subjects(emptySet())
            .title("video title")
            .type(VideoType.NEWS)
            .videoId(VideoId(URI.create("https://video-service.uri/videos/123456789012345678901234")))
            .build()

        doReturn(video)
            .whenever(fakeVideoClient)
            .get(any<VideoId>())
    }
}
