package com.github.unhappychoice.circleci

import org.apache.commons.codec.binary.Hex
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.spekframework.spek2.Spek
import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object WebhookSignatureVerifierSpec : Spek({
    val payload = """{"type":"workflow-completed","id":"example"}""".toByteArray(StandardCharsets.UTF_8)
    val signingSecret = "dummy-signing-secret"
    val signature = hmacSha256Hex(payload, signingSecret)

    test("accepts a signature header matching the payload and signing secret") {
        assertTrue(isValidSignature(payload, "v1=$signature", signingSecret))
    }

    test("accepts a signature header with extra comma-separated entries alongside v1") {
        assertTrue(isValidSignature(payload, "t=1700000000,v1=$signature", signingSecret))
    }

    test("rejects a signature computed for a different payload") {
        val tamperedPayload = """{"type":"workflow-completed","id":"tampered"}""".toByteArray(StandardCharsets.UTF_8)

        assertFalse(isValidSignature(tamperedPayload, "v1=$signature", signingSecret))
    }

    test("rejects a signature computed with a different signing secret") {
        assertFalse(isValidSignature(payload, "v1=$signature", "wrong-signing-secret"))
    }

    test("rejects a header with no v1 entry") {
        assertFalse(isValidSignature(payload, "v2=$signature", signingSecret))
    }

    test("rejects a malformed header") {
        assertFalse(isValidSignature(payload, "not-a-valid-header", signingSecret))
    }

    test("rejects a header with non-hex v1 value") {
        assertFalse(isValidSignature(payload, "v1=not-hex", signingSecret))
    }

    test("rejects a null header") {
        assertFalse(isValidSignature(payload, null, signingSecret))
    }

    test("rejects a blank header") {
        assertFalse(isValidSignature(payload, "   ", signingSecret))
    }
})

private fun hmacSha256Hex(payload: ByteArray, signingSecret: String): String {
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(SecretKeySpec(signingSecret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
    return String(Hex.encodeHex(mac.doFinal(payload)))
}
