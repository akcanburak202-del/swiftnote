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
