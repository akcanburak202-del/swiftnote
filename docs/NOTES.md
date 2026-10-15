# NOTES — cihaz ve platform bulguları

Sadece ekle, en yenisi en altta. Her kayıt: tarih-saat, build SHA, cihaz/OS sürümü, ne çalıştırıldı, ne görüldü. "Doğrulandı" yalnız cihazda görülen şey için; gerisi "çıkarım". Başlığı olmayan sayı yazılmaz.

## 2026-10-14 — başlangıç: MateBridge'den devralınan cihaz gerçekleri

Kaynak: `~/Desktop/matebridge/docs/NOTES.md` ve kararlar (aynı tablet, cihazda gözlenmiş).

- Cihaz MRDI-W09, HarmonyOS 4.3.0.145 (C432E1R1P2), Android uyumluluğu API 31, GMS yok, 2800×1840 @ 360 dpi.
- İlk `adb install` AppGallery risk kontrolünü (`InstallDistActivity`) açar; adb hata dese de kullanıcı onayıyla kurulum başarılı olabilir. `monkey -p` kurulu olmayan paket için AppGallery araması açar. `install -r` güncellemeleri genelde onaysız.
- adb PATH'te değil; adb 37.0.1 mDNS köprüsünde çöktü → sunucu `ADB_MDNS=0 ADB_MDNS_AUTO_CONNECT=0` ile (MateBridge `scripts/usb-mode.sh`). `adb kill-server` `adb reverse` tünellerini düşürür.
- Kablosuz hata ayıklama düğmesi yok: `adb tcpip 5555` + `adb connect <ip>:5555` (yeniden başlatmaya kadar).
- `adb shell input text` Türkçe Q düzeninde `:` → `Ş` yapar.
- Klavye kılıfı: Cmd/Meta uygulamaya ulaşmıyor; Fn ve halka tuşunu sistem yutuyor; Esc hem ESCAPE hem BACK; tuş tekrarı `repeatCount>0`.
- Üçüncü parti IME (SwiftKey) donanım klavyesi harflerini `Activity.dispatchKeyEvent`'ten önce yiyebiliyor.
- Huawei HiWrite (`com.huawei.hiwrite`) görünür her düzenlenebilir `EditText` üstüne el yazısı katmanı koyabiliyor.
- Odaklı bir view dokunma modu dışındayken gri odak vurgusu çiziyor → `defaultFocusHighlightEnabled=false`.
- Arka plandaki MateBridge süreci önbellekte ~%0 CPU tutuluyor. Erişilebilirlik, overlay ve pil optimizasyonu öldürmesi hakkında MateBridge'de **hiç kayıt yok**: Aşama 0'da ilk kez sınanacak.

## 2026-10-14 — T-001 kullanıcı kararları

- Paket adı `dev.swiftnote` (ADR 0003). Kısayol `Ctrl+Shift+N` (ADR 0002). Depo public GitHub, CI ile (ADR 0005).

## 2026-10-14 — T-001: SDK seviyesi (doğrulandı, cihazda)

- Build: yok (salt okuma). Çalıştırılan: `adb shell getprop ro.build.version.sdk` / `ro.product.brand` / `ro.build.display.id`; bağlantı kablosuz adb (`adb tcpip`).
- Sonuç: SDK **31**, marka `HUAWEI`, `MRDI-W09 4.3.0.145(C432E1R1P2)`. ≥33 değil → ADR 0002'ye Android 13 kısıtlı ayar notu gerekmiyor.

## 2026-10-15 — T-001: ilk kurulum (doğrulandı, cihazda)

- Build `98ae409` (`daily`, versionCode 413741, `versionName=0.1-98ae409`). Çalıştırılan: `./scripts/install-apk.sh`.
- Bağlantı: tablet ev ağında değildi; LAN adresine `adb connect` reddedildi, Tailscale adresine `adb connect <tailscale-ip>:5555` bağlandı (önceden açılmış `adb tcpip 5555` hâlâ etkindi). Kurulum Tailscale üzerinden ~10 sn.
- İlk kurulumda iki onay çıktı; betik ikisine de paket + resource-id ile kendisi bastı (`com.android.packageinstaller` `android:id/button1`, `com.huawei.appmarket` `hidden_card_install_button_continue`). Kullanıcının dokunması gerekmedi. `adb install` çıktısı `Success`; `lastUpdateTime` boş → `2026-10-15 10:41:43`.
- `pkgFlags` içinde `DEBUGGABLE` yok (daily derlemesi gerçekten debuggable değil). `/sdcard/sn-ui.xml` kurulumdan sonra yok (Codex düzeltmesi çalışıyor). `MainActivity` öne geldi; ekran görüntüsünde "SwiftNote / build 98ae409 / 2026-10-15T07:41Z".

## 2026-10-15 — T-006: paket algılama (doğrulandı, cihazda)

- Build: storage probe (`dev.swiftnote.probe.storage`). Çalıştırılan: `./scripts/install-apk.sh <probe apk>`.
- Betik paket adını ve başlatılabilir activity'yi `aapt2 dump badging` ile apk'dan okudu, doğru paketi kurup `lastUpdateTime` ile doğruladı.
- Ekran uykudayken (`mWakefulness=Asleep`) kurulum onay ekranı görünmüyor ve betik zaman aşımına uğruyor. Kurulumdan önce ekran açık olmalı.

## 2026-10-15 — T-004: depolama (kısmi, cihazda)

- Doğrulandı: `MANAGE_EXTERNAL_STORAGE` izni verilebiliyor; Türkçe karakterli ve `—` içeren başlıkla dosya oluştu, frontmatter ve başlık doğru, ilk madde `- **00:12** …` biçiminde.
- Sınanmadı: aynı dosyaya ikinci not, Mac/Obsidian tarafında görünme, pano.

## 2026-10-15 — T-002: tuş filtresi ve kutu (kısmi, cihazda)

- Build: keys probe, `main` `cedbcc6` (`dev.swiftnote.probe.keys`, kullanıcı kurdu, `lastUpdateTime=2026-10-15 11:35:14`). Yol A (`TYPE_ACCESSIBILITY_OVERLAY`), overlay izni yok.
- Doğrulandı: YouTube öndeyken Ctrl+Shift+N servise ulaşıyor ve kutu açılıyor. YouTube dışında filtre OFF, diğer kısayollar çalışıyor, YouTube dışı tuş sayacı 0.
- Meta değerleri (klavye kılıfı): Num Lock açık `0x200000`; Shift `+0x41`; Ctrl `+0x3000`. Ctrl+Shift+N = `meta=0x203041`; YouTube'da Shift+Enter = `key=66 meta=0x200041` (Shift ulaşıyor).
- Kutu içinde Shift+Enter ile Enter aynı davranıyor ve "son kaydedilen not" boş kalıyor → Enter kutunun kaydetme yoluna hiç ulaşmıyor. Çıkarım: IME (`com.huawei.ohos.inputmethod`, Celia) donanım Enter'ını `EditText` içinde işliyor. Aşama 1'de `dispatchKeyEventPreIme` ile IME'den önce yakalanacak.
- **Üç parmakla yukarı kaydırma (dokunmatik yüzey → ana ekran) servis açıkken YouTube'da çalışmıyor;** filtrenin kapalı olduğu diğer uygulamalarda çalışıyor; servis kapatılınca YouTube'da da çalışıyor. Yani bozan şey filtrenin açık olması. Hareketin tuş olayı mı dokunmatik yüzey olayı mı olduğu henüz ayrılmadı.
- Huawei logcat'te `Log.i` görünmüyor (probe'un geçiş logu boş geldi). Erişilebilirlik servisi açılmadan önce cihazda açık başka servis yoktu.
