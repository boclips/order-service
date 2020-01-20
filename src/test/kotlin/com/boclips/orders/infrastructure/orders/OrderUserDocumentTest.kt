package com.boclips.orders.infrastructure.orders

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import testsupport.TestFactories

class OrderUserDocumentTest {
    @Nested
    inner class BasicUser {
        @Test
        fun `is a basic user when label is set`() {
            val userDocument = TestFactories.basicOrderUserDocument(label = "hi")

            assertTrue(userDocument.isBasicUser())
        }
    }

    @Nested
    inner class CompleteUser {
        @Test
        fun `is complete user when all data is set`() {
            val userDocument = TestFactories.orderUserDocument()

            assertTrue(userDocument.isCompleteUser())
        }
    }
}