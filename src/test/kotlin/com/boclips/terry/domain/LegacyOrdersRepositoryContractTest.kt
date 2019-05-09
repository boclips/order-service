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

}

@Disabled
abstract class LegacyOrdersRepositoryTests {
    lateinit var repo: LegacyOrdersRepository

    @Test
    fun `creates a legacy order`() {
        repo.clear()
        val legacyOrder = LegacyOrder(legacyId = ObjectId().toHexString())
        repo.add(legacyOrder)
        assertThat(repo.findAll()).containsExactly(legacyOrder)
    }
}
