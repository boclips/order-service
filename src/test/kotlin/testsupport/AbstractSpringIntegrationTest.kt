package testsupport

import com.boclips.eventbus.infrastructure.SynchronousFakeEventBus
import com.boclips.orders.application.cart.AddItemToCart
import com.boclips.orders.domain.model.LegacyOrdersRepository
import com.boclips.orders.domain.model.Order
import com.boclips.orders.domain.model.cart.Cart
import com.boclips.orders.domain.model.cart.CartItem
import com.boclips.orders.domain.service.EmailSender
import com.boclips.orders.infrastructure.carts.MongoCartsRepository
import com.boclips.orders.infrastructure.orders.MongoOrdersRepository
import com.boclips.orders.infrastructure.orders.TestMongoProcess
import com.boclips.videos.api.httpclient.test.fakes.ChannelsClientFake
import com.boclips.videos.api.httpclient.test.fakes.VideosClientFake
import com.boclips.videos.api.request.video.StreamPlaybackResource
import com.boclips.videos.api.response.HateoasLink
import com.boclips.videos.api.response.channel.ChannelResource
import com.boclips.videos.api.response.video.CaptionStatus
import com.boclips.videos.api.response.video.PriceResource
import com.boclips.videos.api.response.video.VideoResource
import com.boclips.videos.api.response.video.VideoTypeResource
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
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

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
    lateinit var mongoCartsRepository: MongoCartsRepository

    @Autowired
    lateinit var fakeVideoClient: VideosClientFake

    @Autowired
    lateinit var fakeChannelsClient: ChannelsClientFake

    @Autowired
    lateinit var legacyOrdersRepository: LegacyOrdersRepository

    @Autowired
    lateinit var eventBus: SynchronousFakeEventBus

    @Autowired
    lateinit var addItemToCart: AddItemToCart

    @Autowired
    lateinit var emailSender: EmailSender

    @BeforeEach
    fun setup() {
        ordersRepository.deleteAll()
        mongoCartsRepository.deleteAll()
        legacyOrdersRepository.clear()
    }

    @AfterEach
    fun tearDown() {
        fakeVideoClient.clear()
        fakeChannelsClient.clear()
        eventBus.clearState()
    }

    fun defaultVideoClientResponse(
        videoId: String = "video-service-id",
        channelId: String = "content-partner-id",
        channelName: String = "our-content-partner",
        channelCurrency: Currency? = Currency.getInstance("GBP"),
        price: PriceResource = PriceResource(amount = BigDecimal(600), currency = Currency.getInstance("GBP")),
        types: List<VideoTypeResource> = listOf(VideoTypeResource(id = 1, name = "STOCK"))
    ) {
        fakeVideoClient.add(
            VideoResource(
                id = videoId,
                title = "hippos are cool",
                channelVideoId = "abc-123",
                channelId = channelId,
                playback = StreamPlaybackResource(id = "playback-id", referenceId = "ref-id"),
                releasedOn = LocalDate.now(),
                createdBy = "creat0r",
                price = price,
                types = types,
                _links = mapOf("fullProjection" to HateoasLink("https://great-vids.com"))
            )
        )

        fakeVideoClient.updateCaptionStatus(videoId, CaptionStatus.HUMAN_GENERATED_AVAILABLE)

        fakeChannelsClient.add(
            ChannelResource(
                id = channelId,
                name = channelName,
                currency = channelCurrency?.currencyCode,
                distributionMethods = emptySet(),
                official = true
            )
        )
    }

    fun saveOrder(order: Order = OrderFactory.completeOrder()): Order {
        return ordersRepository.save(order)
    }

    fun createCart(userId: String, items: List<CartItem> = emptyList()): Cart {
        return mongoCartsRepository.create(CartFactory.sample(userId = userId, items = items))
    }

    fun saveItemToCart(videoId: String, userId: String): CartItem {
        return addItemToCart(videoId, userId)
    }
}
