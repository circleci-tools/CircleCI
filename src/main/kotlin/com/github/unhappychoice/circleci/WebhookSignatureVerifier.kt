package com.github.unhappychoice.circleci

import org.apache.commons.codec.binary.Hex
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_ALGORITHM = "HmacSHA256"
private const val SIGNATURE_VERSION = "v1"
private val signatureEntry = Regex("""[ \t]*([A-Za-z0-9_-]+)[ \t]*=[ \t]*([\x21-\x7E&&[^=]]+)[ \t]*""")
private val v1Digest = Regex("[0-9a-fA-F]{64}")

/**
 * Verifies a CircleCI outbound webhook delivery: recomputes the HMAC-SHA256 digest of the raw
 * [payload] bytes keyed by [signingSecret], and compares it, in constant time, against the `v1`
 * entry of the `circleci-signature` header
 * (https://circleci.com/docs/outbound-webhooks/#verifying-the-signature).
 * Returns false if [signingSecret] is empty or if the header is null, blank, malformed,
 * or missing a `v1` entry.
 * [payload] must be the exact bytes CircleCI sent; re-encoding a parsed body can shift the digest.
 */
fun isValidSignature(payload: ByteArray, signatureHeader: String?, signingSecret: String): Boolean {
    if (signingSecret.isEmpty()) return false
    val providedSignature = parseSignatureHeader(signatureHeader) ?: return false
    val expectedSignature = computeSignature(payload, signingSecret)
    return MessageDigest.isEqual(expectedSignature, providedSignature)
}

private fun computeSignature(payload: ByteArray, signingSecret: String): ByteArray {
    val mac = Mac.getInstance(HMAC_ALGORITHM)
    mac.init(SecretKeySpec(signingSecret.toByteArray(StandardCharsets.UTF_8), HMAC_ALGORITHM))
    return mac.doFinal(payload)
}

private fun parseSignatureHeader(signatureHeader: String?): ByteArray? {
    if (signatureHeader.isNullOrBlank()) return null
    val entries = signatureHeader.split(",").map { entry ->
        signatureEntry.matchEntire(entry)?.destructured?.let { (key, value) -> key to value } ?: return null
    }
    val (_, digest) = entries.singleOrNull { (key, _) -> key == SIGNATURE_VERSION } ?: return null
    return if (v1Digest.matches(digest)) Hex.decodeHex(digest) else null
}
