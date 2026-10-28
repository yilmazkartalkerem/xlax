package com.xlax.launcher

object Config {
    /** !!! Kendi sunucu adresini yaz (durum göstergesi için) */
    const val SERVER_HOST = "oyna.sunucum.com"
    const val CHANNEL_URL = "https://www.youtube.com/@Kartal-CC37"
    /** Boş bırakılırsa kanal ID'si adresten otomatik bulunur. İstersen "UC..." yaz. */
    const val CHANNEL_ID = ""
    const val SLIDE_MS = 5000L      // kartın değişme süresi
    const val CLIP_MS = 3000L       // klip süresi / yenilenme aralığı
    const val CLIP_START_SEC = 10   // klibin videonun kaçıncı saniyesinden başlayacağı
    const val MAX_VIDEOS = 4        // 1 kanal kartı + 4 video = 5 nokta
}
