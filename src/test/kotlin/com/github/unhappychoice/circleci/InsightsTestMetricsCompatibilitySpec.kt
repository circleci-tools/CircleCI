package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.TestMetricsResponse.FailedTest
import com.github.unhappychoice.circleci.v2.response.TestMetricsResponse.SlowTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object InsightsTestMetricsCompatibilitySpec : Spek({
    val defaultConstructorMarker = Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
    val string = String::class.java
    val long = Long::class.javaPrimitiveType!!
    val int = Int::class.javaPrimitiveType!!

    test("retains the legacy failed test constructor with a default job name") {
        val constructor = FailedTest::class.java.getConstructor(string, string, string, long, long, string, int, defaultConstructorMarker)
        val result = constructor.newInstance("addsNumbers", "CalculatorTest", "CalculatorTest.kt", 1L, 2L, "ignored", 0b100000, null)

        assertEquals(FailedTest("addsNumbers", "CalculatorTest", "CalculatorTest.kt", 1L, 2L, p95Duration = null), result)
    }

    test("retains the legacy slow test constructor with a default job name") {
        val constructor = SlowTest::class.java.getConstructor(string, string, string, Float::class.javaObjectType, string, int, defaultConstructorMarker)
        val result = constructor.newInstance("generatesReport", "ReportTest", "ReportTest.kt", 2.5f, "ignored", 0b10000, null)

        assertEquals(SlowTest("generatesReport", "ReportTest", "ReportTest.kt", 2.5f, failedRuns = 0), result)
    }

    test("preserves failed test metrics through the legacy JVM copy with default arguments") {
        val original = FailedTest("addsNumbers", "CalculatorTest", "CalculatorTest.kt", 1L, 2L, "test", 0.1, true, "junit")
        val type = FailedTest::class.java
        val copy = type.getMethod("copy\$default", type, string, string, string, long, long, string, int, Any::class.java)
        val result = copy.invoke(null, original, null, null, null, 0L, 0L, null, 0b111111, null)

        assertEquals(original, result)
    }

    test("preserves slow test metrics through the legacy JVM copy with default arguments") {
        val original = SlowTest("generatesReport", "ReportTest", "ReportTest.kt", 2.5f, "test", 1L, 3L, true, "junit")
        val type = SlowTest::class.java
        val copy = type.getMethod("copy\$default", type, string, string, string, Float::class.javaObjectType, string, int, Any::class.java)
        val result = copy.invoke(null, original, null, null, null, null, null, 0b11111, null)

        assertEquals(original, result)
    }
})
