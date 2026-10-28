package com.xlax.launcher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AuthActivity : AppCompatActivity() {
    private var register = false
    private var gender = "none"
    private lateinit var chips: List<Pair<TextView, String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        if (Store.session(this) != null) { openMain(); return }
        setContentView(R.layout.activity_auth)

        val tabLogin = findViewById<TextView>(R.id.tabLogin)
        val tabReg = findViewById<TextView>(R.id.tabRegister)
        val regGroup = findViewById<View>(R.id.regGroup)
        val submit = findViewById<TextView>(R.id.btnSubmit)
        val err = findViewById<TextView>(R.id.tvError)
        val user = findViewById<EditText>(R.id.etUser)
        val pass = findViewById<EditText>(R.id.etPass)
        val pass2 = findViewById<EditText>(R.id.etPass2)

        chips = listOf(findViewById<TextView>(R.id.gMale) to "male",
            findViewById<TextView>(R.id.gFemale) to "female",
            findViewById<TextView>(R.id.gNone) to "none")
        chips.forEach { (v, g) -> v.setOnClickListener { gender = g; refreshChips() } }
        refreshChips()

        fun setMode(reg: Boolean) {
            register = reg
            tabLogin.isSelected = !reg; tabReg.isSelected = reg
            tabLogin.setTextColor(if (!reg) Color.parseColor("#14110A") else Color.parseColor("#8A8F9C"))
            tabReg.setTextColor(if (reg) Color.parseColor("#14110A") else Color.parseColor("#8A8F9C"))
            regGroup.visibility = if (reg) View.VISIBLE else View.GONE
            submit.text = if (reg) "Hesap oluştur" else "Giriş yap"
            err.visibility = View.GONE
        }
        tabLogin.setOnClickListener { setMode(false) }
        tabReg.setOnClickListener { setMode(true) }
        setMode(true) // görseldeki gibi "Hesap oluştur" ile açılır

        submit.setOnClickListener {
            val name = user.text.toString().trim()
            val pw = pass.text.toString()
            val error = when {
                !Regex("^[A-Za-z0-9_]{3,16}$").matches(name) -> "Kullanıcı adı 3-16 karakter olmalı (harf, rakam, _)."
                pw.length < 6 -> "Şifre en az 6 karakter olmalı."
                register && pw != pass2.text.toString() -> "Şifreler eşleşmiyor."
                register -> Store.register(this, name, pw, gender)
                else -> Store.login(this, name, pw)
            }
            if (error != null) { err.text = error; err.visibility = View.VISIBLE } else openMain()
        }
    }

    private fun refreshChips() = chips.forEach { (v, g) -> v.isSelected = g == gender }

    private fun openMain() { startActivity(Intent(this, MainActivity::class.java)); finish() }
}
