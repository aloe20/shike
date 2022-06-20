@file:Suppress("unused")

package com.aloe.shike.common

import android.annotation.SuppressLint
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * byte数组转十六进制字符串.
 * @return 十六进制字符串
 */
fun toHexString(): String = StringBuilder().run {
    forEach { append(String.format("%02X", it)) }
    toString()
}

/**
 * 十六进制字符串转byte数组.
 * @return byte数组
 */
fun String.toByteArray(): ByteArray = ByteArray(length.shr(1)).also {
    for (index in it.indices) {
        it[index] = substring(index.shl(1), (index + 1).shl(1)).toShort(16).toByte()
    }
}

/**
 * 数据摘要签名, 默认MD5签名.
 * @param type 摘要类型，如MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512
 * @return 签名结果
 */
fun ByteArray.sign(type: String = "MD5"): ByteArray = MessageDigest.getInstance(type).digest(this)

/**
 * Hmac摘要签名, 默认HmacMD5.
 * @param key 密钥
 * @param type 摘要类型，如HmacMD5, HmacSHA1, HmacSHA224, HmacSHA256, HmacSHA384, HmacSHA512
 * @return 签名结果
 */
fun ByteArray.sign(key: ByteArray, type: String): ByteArray = Mac.getInstance(type).let {
    it.init(SecretKeySpec(key, it.algorithm))
    it.doFinal(this)
}

/**
 * Base加密.
 * @param flags 转换类型
 * @return 加密后的内容
 */
fun ByteArray.base64(flags: Int = Base64.DEFAULT): String = Base64.encodeToString(this, flags)

object Encrypt {

    /**
     * AES加密解密.
     * @param array 加密解密内容
     * @param key 密钥，必须16位，如0000111122223333
     * @param type 加密模式，只支持ECB，CBC，CRT，OFB和CFB
     * @param limit 偏移量，除ECB模式外，其它均需传偏移量，如1234567890123456
     * @param decrypt 加密为false，解密为true
     */
    @SuppressLint("GetInstance")
    fun aes(
        array: ByteArray,
        key: String,
        type: String = "ECB",
        limit: String = "",
        decrypt: Boolean = false
    ): ByteArray = Cipher.getInstance("AES/$type/PKCS5PADDING").run {
        init(
            if (decrypt) Cipher.DECRYPT_MODE else Cipher.ENCRYPT_MODE,
            SecretKeySpec(key.toByteArray(), "AES"),
            if ("ECB" == type) null else IvParameterSpec(limit.toByteArray())
        )
        doFinal(array)
    }

    /**
     * 生成RSA密钥对.
     * @param length 密码长度，如512,1024,2048
     * @return 密钥对，公钥: [KeyPair.getPublic]，私钥: [KeyPair.getPrivate]
     */
    fun generateRsaKeyPair(length: Int = 1024): KeyPair = KeyPairGenerator.getInstance("RSA").run {
        initialize(length)
        genKeyPair()
    }

    /**
     * RSA加密解密.
     * @param isPublicKey true为公钥加密解密，false为私钥加密解密
     * @param content 待加密解密内容
     * @param key 公钥或私钥
     * @param decrypt true解密，false加密，默认为加密
     */
    fun rsa(
        isPublicKey: Boolean,
        content: ByteArray,
        key: ByteArray,
        decrypt: Boolean = false
    ): ByteArray {
        val keyFactory = KeyFactory.getInstance("RSA")
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        val mode = if (decrypt) Cipher.DECRYPT_MODE else Cipher.ENCRYPT_MODE
        val keyType =
            if (isPublicKey) keyFactory.generatePublic(X509EncodedKeySpec(key)) else keyFactory.generatePrivate(
                PKCS8EncodedKeySpec(key)
            )
        cipher.init(mode, keyType)
        return cipher.doFinal(content)
    }
}
