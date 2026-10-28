# XLAX MC Launcher (Android arayüzü)

## APK nasıl alınır
**A) Android Studio:** klasörü aç → Build > Build APK(s).
**B) Bilgisayarsız:** projeyi GitHub'a yükle → Actions > "APK derle" → çıkan `XlaxLauncher-apk` dosyasını indir.

## Yapman gerekenler
1. `Config.kt` içinde `SERVER_HOST` değerini kendi sunucu adresinle değiştir.
2. `GameLauncher.kt` içindeki `start()` fonksiyonuna Pojav tabanlı oyun başlatma çağrısını bağla.

## Davranış
- Kart 5 sn'de bir değişir: kanal kartı + kanalın son 4 videosu (alttaki altın çizgi sayaçtır).
- Video kartında her 3 sn'de yenilenen 3 sn'lik sessiz klip oynar. Karta dokununca video açılır.
- "Kanalı aç" -> youtube.com/@Kartal-CC37
- Pojav kodunu kullanırsan LGPL-3.0 gereği kaynak kodunu paylaşmalısın.
