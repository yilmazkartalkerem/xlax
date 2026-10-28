package com.xlax.launcher

import android.content.Context
import android.widget.Toast

/**
 * OYNA! butonunun bağlandığı yer.
 * Bu proje yalnızca arayüzdür. Minecraft'ı gerçekten başlatmak için Pojav tabanlı
 * motorun (JVM + renderer) bu projeye eklenmesi gerekir; oyun başlatma çağrını buraya koy.
 */
object GameLauncher {
    fun start(ctx: Context, username: String) {
        Toast.makeText(ctx, "Oyun motoru henüz bağlı değil ($username)", Toast.LENGTH_SHORT).show()
    }
}
