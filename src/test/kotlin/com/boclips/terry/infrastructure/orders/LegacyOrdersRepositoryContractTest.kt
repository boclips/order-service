package com.boclips.terry.infrastructure.orders

import com.boclips.terry.domain.model.LegacyOrdersRepository
import com.boclips.terry.domain.model.OrderId
import de.flapdoodle.embed.mongo.MongodProcess
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class FakeLegacyOrdersRepositoryTests : OrdersRepositoryTests() {

    @BeforeEach
    override fun setUp() {
        repo = FakeOrdersRepository()
        super.setUp()
    }
}

class MongoLegacyOrdersRepositoryTests : OrdersRepositoryTests() {
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
        repo = MongoOrdersRepository("mongodb://localhost/test")
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

        assertThat(repo.findById(OrderId(value = legacyOrder.id))).isEqualTo(legacyOrderDocument)
    }
}
