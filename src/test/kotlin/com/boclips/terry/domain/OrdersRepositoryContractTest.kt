package com.boclips.terry.domain

import com.boclips.terry.infrastructure.MongoOrdersRepository
import com.mongodb.MongoClientURI
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongoCmdOptionsBuilder
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import mu.KLogging
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.litote.kmongo.KMongo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ActiveProfiles("test")
class OrdersRepositoryTest {
    lateinit var repo: OrdersRepository
    var mongoProcess: MongodProcess? = null

    @BeforeEach
    internal fun setUp() {
        repo = MongoOrdersRepository("mongodb://localhost/test")
        if (mongoProcess == null) {
            mongoProcess = TestMongoProcess.process
        }
    }

    @Test
    fun `creates an order`() {
        val order = Order(
            id = ObjectId().toHexString()
        )

        repo.add(order)

        assertThat(repo.findAll()).containsExactly(order)
    }
}

object TestMongoProcess {
    val process: MongodProcess by lazy {
        val starter = MongodStarter.getDefaultInstance()
        val host = "localhost"
        val port = MongoProperties.DEFAULT_PORT

        KLogging().logger.info { "Booting up MongoDB ${Version.Main.V3_6} on $host:$port" }

        val mongoConfig = MongodConfigBuilder()
            .version(Version.Main.V3_6)
            .cmdOptions(MongoCmdOptionsBuilder().useStorageEngine("ephemeralForTest").build())
            .net(Net(host, port, Network.localhostIsIPv6()))
            .build()

        val mongoExecutable = starter.prepare(mongoConfig)
        mongoExecutable.start()
    }
}
