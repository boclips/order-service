package com.boclips.orders.common

import com.boclips.orders.presentation.carts.TrimServiceRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import java.util.stream.Stream

class TrimmingValidatorTest {

    @ParameterizedTest
    @ArgumentsSource(TrimServiceRequestProvider::class)
    fun `should validate trimming request`(input: TrimServiceRequest, result: Boolean) {
        val trimmingRequest = Specified(input)
        println(input)
        Assertions.assertThat(TrimmingValidator().isValid(trimmingRequest, null)).isEqualTo(result)
    }

    internal class TrimServiceRequestProvider : ArgumentsProvider {
        override fun provideArguments(context: ExtensionContext): Stream<out Arguments?> {
            return Stream.of(
                Arguments.of(TrimServiceRequest(from = "1:00", to = "2:00"), true),
                Arguments.of(TrimServiceRequest(from = "120:00", to = "240:00"), true),
                Arguments.of(TrimServiceRequest(from = "0:00", to = "240:00"), true),
                Arguments.of(TrimServiceRequest(from = "10:00", to = "2:00"), false),
                Arguments.of(TrimServiceRequest(from = "1000", to = "20:00"), false),
                Arguments.of(TrimServiceRequest(from = "10:00", to = "2[:00"), false),
                Arguments.of(TrimServiceRequest(from = "10:00", to = "2o:00"), false),
                Arguments.of(TrimServiceRequest(from = "10:00", to = "00"), false),
                Arguments.of(TrimServiceRequest(from = "10:00", to = "20:61"), false),
                Arguments.of(TrimServiceRequest(from = "10:00", to = "20:1"), false),
                Arguments.of(TrimServiceRequest(from = "10:00", to = null), false),
                Arguments.of(TrimServiceRequest(from = null, to = null), false)
            )
        }
    }
}
