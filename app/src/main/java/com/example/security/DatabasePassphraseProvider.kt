package com.example.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Provides the passphrase used to encrypt the local SQLCipher database.
 *
 * SECURITY FIX: the original code stored the DB passphrase as a hardcoded plaintext
 * string constant in [com.example.data.local.AppDatabase]. Anyone who decompiled the APK
 * could read it directly, which defeats the purpose of an "encrypted" database.
 *
 * Instead, this class generates a random 256-bit passphrase the first time the app runs,
 * and stores it inside [EncryptedSharedPreferences], whose own encryption key is generated
 * and held by the Android Keystore (hardware-backed on most devices) rather than in source
 * code. The database passphrase never appears in the APK and never leaves the device.
 *
 * Note: because the previous hardcoded key is being replaced, any database created by an
 * earlier build of this app cannot be opened with the new passphrase. This build deliberately
 * fails closed instead of silently deleting that database. A production migration from an
 * already-shipped legacy key must be handled as a one-time authenticated data migration.
 */
object DatabasePassphraseProvider {

    private const val PREFS_FILE_NAME = "secure_db_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"
    private const val PASSPHRASE_BYTE_LENGTH = 32 // 256-bit key

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val existing = encryptedPrefs.getString(KEY_DB_PASSPHRASE, null)
        if (existing != null) {
            return existing.toByteArray(Charsets.ISO_8859_1)
        }

        val random = SecureRandom()
        val newPassphraseBytes = ByteArray(PASSPHRASE_BYTE_LENGTH)
        random.nextBytes(newPassphraseBytes)
        val asString = String(newPassphraseBytes, Charsets.ISO_8859_1)

        encryptedPrefs.edit().putString(KEY_DB_PASSPHRASE, asString).apply()

        return newPassphraseBytes
    }
}
