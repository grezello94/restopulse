package com.redlantern.restopulse.security

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyGuard @Inject constructor() {
    fun sha256(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun maskPhone(number: String): String =
        if (number.length <= 4) "****" else "******${number.takeLast(4)}"
}
