package com.powergrid.exemployee.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("unused")
sealed class BiometricResult {
    data class Success(val data: ByteArray) : BiometricResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            return data.contentEquals(other.data)
        }
        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }
    data class Error(val message: String)   : BiometricResult()
    object Cancelled    : BiometricResult()
    object NotAvailable : BiometricResult()
}

@Singleton
class BiometricHelper @Inject constructor(@param:ApplicationContext private val context: Context) {
    companion object {
        private const val KEY_ALIAS       = "exemployee_bio_key"
        private const val KEYSTORE        = "AndroidKeyStore"
        private const val PREFS           = "bio_prefs"
        private const val KEY_CIPHER_TEXT = "bio_cipher"
        private const val KEY_IV          = "bio_iv"
        private const val KEY_ENABLED     = "bio_enabled"
    }

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isAvailable(): Boolean {
        val mgr = BiometricManager.from(context)
        val authenticators = if (Build.VERSION.SDK_INT >= 30)
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        else
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        return mgr.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun isEnabled()       = prefs.getBoolean(KEY_ENABLED, false)
    fun setEnabled(v: Boolean) = prefs.edit { putBoolean(KEY_ENABLED, v) }

    fun hasStoredSecret() =
        !prefs.getString(KEY_CIPHER_TEXT, null).isNullOrBlank() &&
        !prefs.getString(KEY_IV, null).isNullOrBlank()

    fun promptToEncryptAndStore(activity: FragmentActivity, secret: String, onResult: (BiometricResult) -> Unit) {
        val cipher = buildEncryptCipher() ?: run { onResult(BiometricResult.Error("Key generation failed")); return }
        val executor = ContextCompat.getMainExecutor(activity)
        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                val c = r.cryptoObject?.cipher ?: run { onResult(BiometricResult.Error("No cipher")); return }
                val encrypted = c.doFinal(secret.toByteArray(Charsets.UTF_8))
                prefs.edit {
                    putString(KEY_CIPHER_TEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                    putString(KEY_IV, Base64.encodeToString(c.iv, Base64.NO_WRAP))
                    putBoolean(KEY_ENABLED, true)
                }
                onResult(BiometricResult.Success(ByteArray(0)))
            }
            override fun onAuthenticationError(code: Int, msg: CharSequence) {
                onResult(if (code == BiometricPrompt.ERROR_USER_CANCELED || code == BiometricPrompt.ERROR_NEGATIVE_BUTTON)
                    BiometricResult.Cancelled else BiometricResult.Error(msg.toString()))
            }
        }).authenticate(buildPromptInfo("Enable Biometric Login"), BiometricPrompt.CryptoObject(cipher))
    }

    fun promptToDecrypt(activity: FragmentActivity, onResult: (BiometricResult) -> Unit) {
        val cipherText = prefs.getString(KEY_CIPHER_TEXT, null) ?: run { onResult(BiometricResult.Error("Nothing stored")); return }
        val ivStr      = prefs.getString(KEY_IV, null)          ?: run { onResult(BiometricResult.Error("No IV")); return }
        val iv         = Base64.decode(ivStr, Base64.NO_WRAP)
        val cipher     = buildDecryptCipher(iv)                 ?: run { onResult(BiometricResult.Error("Cipher init failed")); return }
        val executor   = ContextCompat.getMainExecutor(activity)
        BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(r: BiometricPrompt.AuthenticationResult) {
                val c = r.cryptoObject?.cipher ?: run { onResult(BiometricResult.Error("No cipher")); return }
                onResult(BiometricResult.Success(c.doFinal(Base64.decode(cipherText, Base64.NO_WRAP))))
            }
            override fun onAuthenticationError(code: Int, msg: CharSequence) {
                onResult(if (code == BiometricPrompt.ERROR_USER_CANCELED || code == BiometricPrompt.ERROR_NEGATIVE_BUTTON)
                    BiometricResult.Cancelled else BiometricResult.Error(msg.toString()))
            }
        }).authenticate(buildPromptInfo("Biometric Sign-In"), BiometricPrompt.CryptoObject(cipher))
    }

    fun clearSecret() = prefs.edit { remove(KEY_CIPHER_TEXT); remove(KEY_IV); putBoolean(KEY_ENABLED, false) }

    private fun buildPromptInfo(title: String) = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title).setSubtitle("Confirm your identity")
        .setNegativeButtonText("Cancel")
        .setAllowedAuthenticators(if (Build.VERSION.SDK_INT >= 30)
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        else BiometricManager.Authenticators.BIOMETRIC_WEAK)
        .build()

    private fun buildEncryptCipher(): Cipher? = runCatching {
        Cipher.getInstance("AES/CBC/PKCS7Padding").also { it.init(Cipher.ENCRYPT_MODE, getOrCreateKey()) }
    }.getOrNull()

    private fun buildDecryptCipher(iv: ByteArray): Cipher? = runCatching {
        Cipher.getInstance("AES/CBC/PKCS7Padding").also { it.init(Cipher.DECRYPT_MODE, getOrCreateKey(), IvParameterSpec(iv)) }
    }.getOrNull()

    private fun getOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE).also { it.load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)
                .also { if (Build.VERSION.SDK_INT >= 30) it.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG) }
                .setInvalidatedByBiometricEnrollment(true)
                .build())
        }.generateKey()
    }
}
