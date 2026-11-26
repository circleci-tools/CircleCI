package com.github.unhappychoice.circleci

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
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

    test("falls back to declared properties when the class has no primary constructor") {
        val model = NoPrimaryConstructorModel("example", "dummy-secret")

        val description = model.toString()
        assertTrue(description.startsWith("NoPrimaryConstructorModel("))
        assertTrue(description.contains("name=example"))
        assertTrue(description.contains("secret=[REDACTED]"))
        assertFalse(description.contains("dummy-secret"))
    }

    test("reuses cached per-class metadata instead of re-deriving it on every call") {
        val kClass = RedactionSpecModel::class
        redactionMetadataCache.remove(kClass)

        redactedToString(RedactionSpecModel(name = "example", secret = "dummy-secret", count = 1))
        val fieldsAfterFirstCall = redactionMetadataCache[kClass]
        assertTrue(fieldsAfterFirstCall != null)

        redactedToString(RedactionSpecModel(name = "other", secret = "other-secret", count = 2))
        val fieldsAfterSecondCall = redactionMetadataCache[kClass]

        assertTrue(fieldsAfterFirstCall === fieldsAfterSecondCall)
    }
})

private data class RedactionSpecModel(
    val name: String,
    @Redacted val secret: String?,
    val count: Int
) {
    override fun toString() = redactedToString(this)
}

private class NoPrimaryConstructorModel {
    val name: String
    @Redacted val secret: String?

    constructor(name: String, secret: String?) {
        this.name = name
        this.secret = secret
    }

    override fun toString() = redactedToString(this)
}
