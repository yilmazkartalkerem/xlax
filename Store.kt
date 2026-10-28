package com.xlax.launcher

import android.content.Context
import java.security.MessageDigest
import java.util.UUID

/** Hesaplar yalnızca bu cihazda, tuzlu SHA-256 özetiyle saklanır. */
object Store {
    private fun p(c: Context) = c.getSharedPreferences("xlax", Context.MODE_PRIVATE)
    private fun hash(pw: String, salt: String) =
        MessageDigest.getInstance("SHA-256").digest((salt + pw).toByteArray()).joinToString("") { "%02x".format(it) }

    fun session(c: Context): String? = p(c).getString("session", null)
    fun logout(c: Context) = p(c).edit().remove("session").apply()

    fun register(c: Context, name: String, pw: String, gender: String): String? {
        val key = "acc_" + name.lowercase()
        if (p(c).contains(key)) return "Bu kullanıcı adı zaten kayıtlı."
        val salt = UUID.randomUUID().toString().take(8)
        p(c).edit().putString(key, "$name:$salt:${hash(pw, salt)}:$gender").putString("session", name).apply()
        return null
    }

    fun login(c: Context, name: String, pw: String): String? {
        val v = p(c).getString("acc_" + name.lowercase(), null) ?: return "Kullanıcı bulunamadı."
        val (realName, salt, h) = v.split(":")
        if (hash(pw, salt) != h) return "Şifre yanlış."
        p(c).edit().putString("session", realName).apply()
        return null
    }
}
