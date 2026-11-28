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
        assertTrue(isValidSignature(payload, "v2=abc, v1 = ${signature.uppercase()}", signingSecret))
    }

    test("rejects a signature computed for a different payload") {
        val tamperedPayload = """{"type":"workflow-completed","id":"tampered"}""".toByteArray(StandardCharsets.UTF_8)

        assertFalse(isValidSignature(tamperedPayload, "v1=$signature", signingSecret))
    }

    test("rejects a signature computed with a different signing secret") {
        assertFalse(isValidSignature(payload, "v1=$signature", "wrong-signing-secret"))
    }

    test("rejects a well-formed signature header when the signing secret is empty") {
        assertFalse(isValidSignature(payload, "v1=$signature", ""))
    }

    test("rejects a header with no v1 entry") {
        assertFalse(isValidSignature(payload, "v2=$signature", signingSecret))
    }

    test("rejects a malformed header") {
        assertFalse(isValidSignature(payload, "not-a-valid-header", signingSecret))
    }

    test("rejects malformed entries alongside a matching v1 signature") {
        listOf("not-a-pair", "=value", "t=", "t=a=b", "bad key=value", "t=line\nbreak", "").forEach { entry ->
            assertFalse(isValidSignature(payload, "$entry,v1=$signature", signingSecret))
            assertFalse(isValidSignature(payload, "v1=$signature,$entry", signingSecret))
        }
    }

    test("rejects multiple v1 entries") {
        assertFalse(isValidSignature(payload, "v1=$signature,v1=$signature", signingSecret))
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
