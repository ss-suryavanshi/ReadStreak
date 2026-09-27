package com.example.data

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {
    private val secureRandom = SecureRandom()
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        secureRandom.nextBytes(saltBytes)
        return saltBytes.toHexString()
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val salted = password + salt
        val digest = md.digest(salted.toByteArray(Charsets.UTF_8))
        return digest.toHexString()
    }

    private fun ByteArray.toHexString(): String {
        val result = CharArray(size * 2)
        for (i in indices) {
            val v = this[i].toInt() and 0xFF
            result[i * 2] = HEX_CHARS[v ushr 4]
            result[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(result)
    }
}
