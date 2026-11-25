package com.github.unhappychoice.circleci

import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object RedactionSpec : Spek({
    test("renders annotated properties as [REDACTED] and keeps others visible, in declaration order") {
        val model = RedactionSpecModel(name = "example", secret = "dummy-secret", count = 2)

        assertEquals("RedactionSpecModel(name=example, secret=[REDACTED], count=2)", model.toString())
    }

    test("redacts a null sensitive property without printing its value") {
        val model = RedactionSpecModel(name = "example", secret = null, count = 0)

        assertEquals("RedactionSpecModel(name=example, secret=[REDACTED], count=0)", model.toString())
    }
})

private data class RedactionSpecModel(
    val name: String,
    @Redacted val secret: String?,
    val count: Int
) {
    override fun toString() = redactedToString(this)
}
