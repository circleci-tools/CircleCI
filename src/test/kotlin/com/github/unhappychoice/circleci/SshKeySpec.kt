package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v1.response.SSHKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object SshKeySpec : Spek({
  test("the legacy SSHKey model exposes its fingerprint and public key") {
    val key = SSHKey(fingerprint = "aa:bb:cc", public_key = "ssh-rsa AAAA...")

    assertEquals("aa:bb:cc", key.fingerprint)
    assertEquals("ssh-rsa AAAA...", key.public_key)
  }
})
