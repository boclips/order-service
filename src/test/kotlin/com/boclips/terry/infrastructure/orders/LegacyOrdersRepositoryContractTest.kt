package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.LegacyOrdersRepository
import de.flapdoodle.embed.mongo.MongodProcess
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class FakeLegacyOrdersRepositoryTests : LegacyOrdersRepositoryTests() {

    @BeforeEach
    override fun setUp() {
        repo = FakeLegacyOrdersRepository()
        super.setUp()
    }
}

class MongoLegacyOrdersRepositoryTests : LegacyOrdersRepositoryTests() {
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

    @BeforeEach
    override fun setUp() {
        repo = MongoLegacyOrdersRepository("mongodb://localhost/test")
        super.setUp()
    }
}

@Disabled
abstract class LegacyOrdersRepositoryTests {
    lateinit var repo: LegacyOrdersRepository

    @BeforeEach
    open fun setUp() {
        repo.clear()
    }

    @Test
    fun `can create a legacy order`() {
        val legacyOrder = TestFactories.legacyOrder(ObjectId().toHexString())

        val legacyOrderDocument = TestFactories.legacyOrderDocument(
            legacyOrder = legacyOrder
        )

        repo.add(legacyOrderDocument)

        val documents = repo.findAll()
        assertThat(documents).hasSize(1)
        assertThat(documents.first()).isEqualTo(legacyOrderDocument)
    }
}
