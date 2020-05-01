package testsupport

import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.orders.domain.model.LegacyOrdersRepository
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import com.boclips.orders.infrastructure.orders.TestMongoProcess
import com.boclips.videos.api.httpclient.test.fakes.ContentPartnersClientFake
import com.boclips.videos.api.httpclient.test.fakes.VideosClientFake
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.contentpartner.ContentPartnerResource
import com.boclips.videos.api.response.video.VideoResource
import de.flapdoodle.embed.mongo.MongodProcess
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.Link
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import java.time.LocalDate
import java.util.*

@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureMockMvc
@ActiveProfiles("test", "fake-security")
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
    lateinit var fakeVideoClient: VideosClientFake

    @Autowired
    lateinit var fakeContentPartnersClient: ContentPartnersClientFake

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
        fakeContentPartnersClient.clear()
        eventBus.clearState()
    }

    fun defaultVideoClientResponse(
        videoId: String = "123456789012345678901234",
        contentPartnerId: String = "content-partner-id",
        contentPartnerName: String = "our-content-partner",
        contentPartnerCurrency: Currency? = Currency.getInstance("GBP")
    ) {
        fakeVideoClient.add(
            VideoResource(
                id = videoId,
                title = "hippos are cool",
                contentPartnerVideoId = "abc-123",
                contentPartnerId = contentPartnerId,
                playback = StreamPlaybackResource(id = "playback-id", referenceId = "ref-id"),
                releasedOn = LocalDate.now(),
                createdBy = "creat0r",
                _links = mapOf("fullProjection" to Link("https://great-vids.com"))
            )
        )

        fakeContentPartnersClient.add(
            ContentPartnerResource(
                id = contentPartnerId,
                name = contentPartnerName,
                currency = contentPartnerCurrency?.currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )
    }
}
