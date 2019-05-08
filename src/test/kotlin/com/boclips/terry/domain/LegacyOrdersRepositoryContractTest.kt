package com.boclips.terry.domain

import com.boclips.terry.infrastructure.legacyorders.MongoLegacyOrdersRepository
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
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.mongo.MongoProperties

class FakeLegacyOrdersRepositoryTests : LegacyOrdersRepositoryTests() {
    @BeforeEach
    fun setUp() {
        repo = FakeLegacyOrdersRepository()
    }
}

class MongoLegacyOrdersRepositoryTests : LegacyOrdersRepositoryTests() {
    companion object Setup {
        var mongoProcess: MongodProcess? = null

        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            if (mongoProcess == null) {
                mongoProcess = TestMongoProcess.process
            }
        }
    }

    @BeforeEach
    fun setUp() {
        repo = MongoLegacyOrdersRepository("mongodb://localhost/test")
    }

    object TestMongoProcess {
        val process: MongodProcess by lazy {
            val starter = MongodStarter.getDefaultInstance()
            val host = "localhost"
            val port = MongoProperties.DEFAULT_PORT + 1

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
}

@Disabled
abstract class LegacyOrdersRepositoryTests {
    lateinit var repo: LegacyOrdersRepository

    @Test
    fun `creates a legacy order`() {
        val legacyOrder = LegacyOrder(id = ObjectId().toHexString())
        repo.add(legacyOrder)
        assertThat(repo.findAll()).containsExactly(legacyOrder)
    }
}
