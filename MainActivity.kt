package com.xlax.launcher

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ColorStateList
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {
    private val h = Handler(Looper.getMainLooper())
    private var videos = listOf<Video>()
    private var index = 0          // 0 = kanal kartı, 1.. = videolar
    private var tick = 0
    private var anim: ObjectAnimator? = null
    private val thumbs = HashMap<String, Bitmap>()

    private lateinit var slideChannel: View
    private lateinit var slideVideo: View
    private lateinit var ivThumb: ImageView
    private lateinit var web: WebView
    private lateinit var tvTitle: TextView
    private lateinit var progress: ProgressBar
    private lateinit var dots: LinearLayout
    private lateinit var ivAvatar: ImageView
    private lateinit var user: String

    private val nextSlide = Runnable { go(index + 1) }
    private val clipTick = object : Runnable {
        override fun run() { playClip(); h.postDelayed(this, Config.CLIP_MS) }
    }
    private val statusTick = object : Runnable {
        override fun run() { refreshStatus(); h.postDelayed(this, 30_000) }
    }
    private val pickSkin = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        contentResolver.openInputStream(uri)?.use { File(filesDir, "skin.png").outputStream().use { o -> it.copyTo(o) } }
        loadAvatar()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        immersive()
        user = Store.session(this) ?: run { startActivity(Intent(this, AuthActivity::class.java)); finish(); return }
        setContentView(R.layout.activity_main)

        slideChannel = findViewById(R.id.slideChannel); slideVideo = findViewById(R.id.slideVideo)
        ivThumb = findViewById(R.id.ivThumb); web = findViewById(R.id.web)
        tvTitle = findViewById(R.id.tvVideoTitle); progress = findViewById(R.id.progress)
        dots = findViewById(R.id.dots); ivAvatar = findViewById(R.id.ivAvatar)
        findViewById<TextView>(R.id.tvUser).text = user

        web.settings.javaScriptEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.setBackgroundColor(Color.TRANSPARENT)

        findViewById<View>(R.id.btnChannel).setOnClickListener { open(Config.CHANNEL_URL) }
        findViewById<View>(R.id.touch).setOnClickListener { videos.getOrNull(index - 1)?.let { open(it.url) } }
        findViewById<View>(R.id.btnPrev).setOnClickListener { go(index - 1) }
        findViewById<View>(R.id.btnNext).setOnClickListener { go(index + 1) }
        findViewById<View>(R.id.btnPlay).setOnClickListener { GameLauncher.start(this, user) }
        findViewById<View>(R.id.btnSkin).setOnClickListener { pickSkin.launch("image/png") }
        findViewById<View>(R.id.btnSettings).setOnClickListener {
            AlertDialog.Builder(this).setTitle("Ayarlar")
                .setItems(arrayOf("Çıkış yap")) { _, _ -> Store.logout(this); startActivity(Intent(this, AuthActivity::class.java)); finish() }
                .show()
        }

        loadAvatar(); buildDots()
        Thread {
            val list = try { YouTube.latest(this) } catch (e: Exception) { emptyList() }
            list.forEach { v -> try { URL(v.thumb).openStream().use { BitmapFactory.decodeStream(it) }?.let { thumbs[v.id] = it } } catch (_: Exception) {} }
            h.post { videos = list; buildDots(); go(0) }
        }.start()
    }

    override fun onResume() { super.onResume(); if (!::slideChannel.isInitialized) return; go(index); statusTick.run() }
    override fun onPause() {
        super.onPause()
        h.removeCallbacksAndMessages(null); anim?.cancel()
        if (::web.isInitialized) web.loadUrl("about:blank")
    }

    private fun total() = 1 + videos.size

    /** Slayt değiştir: 5 sn sayaç + (videoysa) her 3 sn'de yenilenen 3 sn'lik klip. */
    private fun go(i: Int) {
        h.removeCallbacks(nextSlide); h.removeCallbacks(clipTick); anim?.cancel()
        index = ((i % total()) + total()) % total()
        tick = 0
        val isVideo = index > 0
        slideChannel.visibility = if (isVideo) View.GONE else View.VISIBLE
        slideVideo.visibility = if (isVideo) View.VISIBLE else View.GONE
        if (isVideo) {
            val v = videos[index - 1]
            tvTitle.text = v.title; ivThumb.setImageBitmap(thumbs[v.id])
            clipTick.run()
        } else web.loadUrl("about:blank")
        updateDots()
        if (total() > 1) {
            anim = ObjectAnimator.ofInt(progress, "progress", 0, 1000).apply {
                duration = Config.SLIDE_MS; interpolator = LinearInterpolator(); start()
            }
            h.postDelayed(nextSlide, Config.SLIDE_MS)
        } else progress.progress = 0
    }

    private fun playClip() {
        val v = videos.getOrNull(index - 1) ?: return
        val start = Config.CLIP_START_SEC + tick * (Config.CLIP_MS / 1000).toInt()
        val end = start + (Config.CLIP_MS / 1000).toInt()
        tick++
        val html = """<html><body style="margin:0;background:#000;overflow:hidden"><iframe width="100%" height="100%" frameborder="0"
            src="https://www.youtube.com/embed/${v.id}?autoplay=1&mute=1&controls=0&playsinline=1&rel=0&modestbranding=1&start=$start&end=$end"
            allow="autoplay; encrypted-media"></iframe></body></html>"""
        web.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null)
    }

    private fun buildDots() { dots.removeAllViews(); repeat(total()) { dots.addView(View(this)) }; updateDots() }
    private fun updateDots() {
        val d = resources.displayMetrics.density
        for (i in 0 until dots.childCount) {
            val v = dots.getChildAt(i)
            val active = i == index
            v.layoutParams = LinearLayout.LayoutParams(((if (active) 22 else 8) * d).toInt(), (8 * d).toInt()).apply { marginHorizontal(this, (4 * d).toInt()) }
            v.setBackgroundResource(R.drawable.dot)
            v.backgroundTintList = ColorStateList.valueOf(Color.parseColor(if (active) "#D4A72C" else "#2B303D"))
        }
    }
    private fun marginHorizontal(p: LinearLayout.LayoutParams, m: Int) { p.leftMargin = m; p.rightMargin = m }

    private fun refreshStatus() {
        Thread {
            var online = false; var count = 0
            try {
                val j = JSONObject(URL("https://api.mcsrvstat.us/3/${Config.SERVER_HOST}").readText())
                online = j.optBoolean("online"); count = j.optJSONObject("players")?.optInt("online") ?: 0
            } catch (_: Exception) {}
            h.post {
                findViewById<TextView>(R.id.tvStatus).text = if (online) "Sunucu açık · $count oyuncu" else "Sunucu kapalı"
                findViewById<View>(R.id.dotStatus).backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor(if (online) "#4ADE80" else "#F87171"))
            }
        }.start()
    }

    /** skin.png varsa yüzünü (8x8 + şapka katmanı) avatar yapar. */
    private fun loadAvatar() {
        val f = File(filesDir, "skin.png")
        val skin = if (f.exists()) BitmapFactory.decodeFile(f.path) else null
        if (skin == null || skin.width < 64) { ivAvatar.setImageResource(R.drawable.default_avatar); return }
        val s = skin.width / 64
        val out = Bitmap.createBitmap(8 * s, 8 * s, Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        c.drawBitmap(skin, Rect(8 * s, 8 * s, 16 * s, 16 * s), Rect(0, 0, 8 * s, 8 * s), null)
        c.drawBitmap(skin, Rect(40 * s, 8 * s, 48 * s, 16 * s), Rect(0, 0, 8 * s, 8 * s), null)
        ivAvatar.setImageBitmap(Bitmap.createScaledBitmap(out, 96, 96, false))
    }

    private fun open(url: String) = startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
