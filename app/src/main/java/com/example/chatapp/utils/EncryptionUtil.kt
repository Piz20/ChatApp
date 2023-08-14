package com.example.chatapp.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Base64

import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val AES_ALGORITHM = "AES/GCM/NoPadding"


    fun encrypt(plainText: String, context: Context): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(context))
        val encryptedBytes = cipher.doFinal(plainText.toByteArray())

        // Récupérer l'IV généré par le Cipher et le concaténer avec le message chiffré
        val iv = cipher.iv
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String, context: Context): String {

        val encryptedBytesWithIV = Base64.decode(encryptedText, Base64.DEFAULT)

        val ivSize = 12
        val iv = encryptedBytesWithIV.copyOfRange(0, ivSize)
        val encryptedBytes = encryptedBytesWithIV.copyOfRange(ivSize, encryptedBytesWithIV.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = getSecretKey(context)
        val secretKeySpec = SecretKeySpec(secretKey.encoded, "AES")
        cipher.init(
            Cipher.DECRYPT_MODE, secretKeySpec, GCMParameterSpec(128, iv)
        )
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    private fun getSecretKey(context: Context): SecretKeySpec {
        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName, PackageManager.GET_META_DATA
        )
        val metaData = appInfo.metaData
        val secretKey = metaData.getString("SECRET_KEY_FOR_ENCRYPTION")
        return SecretKeySpec(secretKey!!.toByteArray(), AES_ALGORITHM)

    }
}
