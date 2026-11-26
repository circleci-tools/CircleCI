package com.github.unhappychoice.circleci

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

private const val HMAC_ALGORITHM = "HmacSHA256"
private const val SIGNATURE_VERSION = "v1"

/**
 * Verifies a CircleCI outbound webhook delivery: recomputes the HMAC-SHA256 digest of the raw
 * [payload] bytes keyed by [signingSecret], and compares it, in constant time, against the `v1`
 * entry of the `circleci-signature` header
 * (https://circleci.com/docs/outbound-webhooks/#verifying-the-signature).
 * A null, blank, or malformed header, or one with no `v1` entry, is rejected rather than thrown.
 * [payload] must be the exact bytes CircleCI sent; re-encoding a parsed body can shift the digest.
 */
fun isValidSignature(payload: ByteArray, signatureHeader: String?, signingSecret: String): Boolean {
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
    return signatureHeader.split(",")
        .mapNotNull { entry ->
            val parts = entry.split("=", limit = 2)
            if (parts.size != 2) null else parts[0].trim() to parts[1].trim()
        }
        .firstOrNull { (key, _) -> key == SIGNATURE_VERSION }
        ?.second
        ?.let(::decodeHex)
}

private fun decodeHex(hex: String): ByteArray? = try {
    Hex.decodeHex(hex)
} catch (error: DecoderException) {
    null
}
