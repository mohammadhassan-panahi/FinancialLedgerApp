package com.example.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Stores a hashed 4-digit PIN (never the raw PIN) in an EncryptedSharedPreferences file,
 * separate from the main encrypted Room DB passphrase — this is a fast local unlock check,
 * not the DB encryption key itself.
 */
class PinManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "pin_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun isPinSet(): Boolean = prefs.contains(KEY_PIN_HASH)

    fun setPin(pin: String) {
        require(pin.length == 4 && pin.all(Char::isDigit)) { "PIN باید دقیقاً ۴ رقم باشد" }
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY_PIN_SALT, salt.toHex())
            .putString(KEY_PIN_HASH, hash(pin, salt))
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKED_UNTIL)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val now = System.currentTimeMillis()
        val lockedUntil = prefs.getLong(KEY_LOCKED_UNTIL, 0L)
        if (lockedUntil > now) return false

        val salt = prefs.getString(KEY_PIN_SALT, null)?.hexToBytes()
            ?: return prefs.getString(KEY_PIN_HASH, null) == legacyHash(pin)

        val expected = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val valid = MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            hash(pin, salt).toByteArray(Charsets.UTF_8)
        )
        if (valid) {
            prefs.edit().remove(KEY_FAILED_ATTEMPTS).remove(KEY_LOCKED_UNTIL).apply()
            return true
        }

        val attempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        val lockMillis = when {
            attempts >= 10 -> 30 * 60_000L
            attempts >= 5 -> 5 * 60_000L
            attempts >= 3 -> 30_000L
            else -> 0L
        }
        prefs.edit()
            .putInt(KEY_FAILED_ATTEMPTS, attempts)
            .putLong(KEY_LOCKED_UNTIL, if (lockMillis > 0) now + lockMillis else 0L)
            .apply()
        return false
    }

    fun getLockoutRemainingMillis(): Long =
        (prefs.getLong(KEY_LOCKED_UNTIL, 0L) - System.currentTimeMillis()).coerceAtLeast(0L)

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .remove(KEY_FAILED_ATTEMPTS)
            .remove(KEY_LOCKED_UNTIL)
            .apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    private fun hash(pin: String, salt: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(salt + pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    // Backward-compatible verifier for PINs created by the previous build.
    private fun legacyHash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray {
        require(length % 2 == 0)
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKED_UNTIL = "locked_until"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val SALT_LENGTH = 16
    }
}
