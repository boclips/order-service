package com.boclips.videos.service.testsupport

import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.infrastructure.orders.MongoOrdersRepository
import com.boclips.terry.infrastructure.orders.TestMongoProcess
import com.boclips.videos.service.client.ContentPartner
import com.boclips.videos.service.client.ContentPartnerId
import com.boclips.videos.service.client.Playback
import com.boclips.videos.service.client.Video
import com.boclips.videos.service.client.VideoId
import com.boclips.videos.service.client.VideoType
import com.boclips.videos.service.client.internal.FakeClient
import com.boclips.videos.service.client.spring.MockVideoServiceClient
import de.flapdoodle.embed.mongo.MongodProcess
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.util.Currency

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

    @Autowired
    lateinit var eventBus: SynchronousFakeEventBus

    @BeforeEach
    fun setup() {
        ordersRepository.deleteAll()
        legacyOrdersRepository.clear()
    }

    @AfterEach
    fun tearDown() {
        fakeVideoClient.clear()
        eventBus.clearState()
    }

    fun defaultVideoClientResponse(
        videoId: String = "123456789012345678901234",
        contentPartnerId: String = "content-partner-id",
        contentPartnerName: String = "our-content-partner",
        contentPartnerCurrency: Currency? = Currency.getInstance("GBP")) {
        val video = Video.builder()
            .videoId(VideoId(URI.create("https://fake-video-service.com/videos/$videoId")))
            .contentPartnerId(contentPartnerId)
            .contentPartnerVideoId("video-id")
            .createdBy(contentPartnerName)
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
            .build()

        val contentPartner = ContentPartner.builder()
            .contentPartnerId(ContentPartnerId(contentPartnerId))
            .name(contentPartnerName)
            .currency(contentPartnerCurrency)
            .build()

            fakeVideoClient.createVideo(video)
            fakeVideoClient.createContentPartner(contentPartner)
    }
}
