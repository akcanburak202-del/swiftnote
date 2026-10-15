---
id: T-004
title: "Probe C: dosya yazma ve pano"
status: review
phase: 0
owner: android-dev
depends_on: [T-001]
decisions: [0004]
files:
  - probes/storage/
verify: ./gradlew -p probes/storage assembleDebug testDebugUnitTest
---

## Amaç

Notların `/sdcard/MateBridge/Wi-Fi/Video Notları/` altına, Türkçe karakterli dosya adlarıyla, sona ekleyerek yazılabildiğini ve aynı anda panoya gittiğini; dosyanın Mac'te MateBridge üzerinden göründüğünü doğrulamak. Araştırma: `docs/research/2026-10-14-feasibility.md` §4–5. Biçim: `docs/SPEC.md` "Dosya biçimi".

## Kapsam dışı

- Kısayol, overlay, medya oturumu.

## Kabul kriterleri

- [ ] Ayrı paket (`dev.swiftnote.probe.storage`), `MANAGE_EXTERNAL_STORAGE`; izin yoksa ayar ekranına götüren düğme.
- [ ] Saf Kotlin, JVM testli: dosya adı temizleme (`/ \ : * ? " < > | # ^ [ ]`, Türkçe korunur, uzunluk sınırı), not satırı biçimi (`- **mm:ss** metin`, çok satırlı not girintili), ilk yazımda frontmatter + başlık.
- [ ] Ekranda: başlık, kanal, zaman ve not alanları; "ekle" → dosyaya ekler ve panoya `mm:ss — not` yazar; dosyanın tam yolunu ve son içeriğini gösterir.
- [ ] Yazma hatası ekranda görünür, metin silinmez.
- [ ] `verify` geçiyor.

## Cihazda (orkestratör + kullanıcı)

- [ ] `Şimşek: Ğ ü ö ç — test` başlıklı videoya iki ekleme → tek dosya, iki madde, ad bozulmamış.
- [ ] Mac'te MateBridge menüsü "Tablet dosyalarını aç" → Finder'da dosya görünüyor, Obsidian'da düzgün açılıyor (frontmatter dahil).
- [ ] Pano: Mac'te Cmd+V aynı metni veriyor.
- [ ] HarmonyOS'ta "Tüm dosyalara erişim" ayarının yeri NOTES'a.

## Plan

1. `probes/storage/` ayrı Gradle projesi (kendi settings/build, AGP 9.4.1, compileSdk 37, minSdk/targetSdk 31, arm64-v8a), paket `dev.swiftnote.probe.storage`.
2. Saf Kotlin `NoteFormat.kt`: `sanitizeFileName`, `fileNameFor`, `formatTime`, `noteLine`, `initialContent`, `clipboardText` + JVM testleri.
3. `NoteWriter.kt`: `java.io` ile `Video Notları/` oluştur, dosya yoksa frontmatter+başlık, sonra yalnız sona ekle (`FileOutputStream(append=true)`), hata `Result` ile döner.
4. `MainActivity.kt`: düz View; izin yoksa "Tüm dosyalara erişim" ayar düğmesi; başlık/kanal/zaman/not alanları; "ekle" önce dosyaya yazar, başarılıysa panoya yazar, yol ve son içerik gösterilir; hata görünür, metin silinmez.

## Devir

- **Commit:** son `T-004: storage and clipboard probe` commit'i (`git log -1`)
- **Dokunulan dosyalar:** `probes/storage/` (settings/build.gradle.kts, AndroidManifest, `NoteFormat.kt`, `NoteWriter.kt`, `MainActivity.kt`, `NoteFormatTest.kt`), bu kart.
- **Verify çıktısı (son satırlar):** `./gradlew -p probes/storage assembleDebug testDebugUnitTest` -> `> Task :testDebugUnitTest` ... `BUILD SUCCESSFUL in 2s`, `40 actionable tasks: 40 executed`.
- **Varsayımlar:** Zaman alanı elle yazılan `mm:ss` metni (probe'da medya yok); saat >= 1 ise `h:mm:ss`. Ad parçaları 80 kod noktasına kısalır; boş parça `Başlıksız` / `Kanalsız`. Frontmatter'da kanal yalnız riskli karakter varsa tırnaklanır. Ekleme `FileOutputStream(append=true)` + `fd.sync()`; dosya `\n` ile bitmiyorsa önce `\n` eklenir. Yazma başarılıysa not alanı temizlenir ve panoya yazılır; yazma hatasında metin kalır.
- **Test edilmeyenler / cihazda doğrulanacaklar:** Hepsi cihazda. APK: `probes/storage/build/outputs/apk/debug/` (paket `dev.swiftnote.probe.storage`). İzin düğmesi HarmonyOS'ta doğru ayar ekranını açıyor mu (yoksa genel ekrana düşer; yeri NOTES'a); `Video Notları` klasörü oluşuyor mu; Türkçe adlı iki ekleme tek dosya; Mac'te MateBridge'de görünüm, Obsidian'da frontmatter; Cmd+V. Alanlar `Şimşek: Ğ ü ö ç — test` / `Test Kanalı` ile dolu açılır.
- **Codex düzeltmeleri:** (1) alanlara sabit ID + manifest `configChanges` ile döndürmede metin korunur; (2) yazma/sync/okuma tek iş parçacıklı executor'da, sonuç UI'a döner, ekle düğmesi yazarken kapalı; (3) tüm dosya adı UTF-8 <= 255 bayt (kanal <= 80 bayt, başlık kalanı), test eklendi. Verify yeniden: BUILD SUCCESSFUL, 13 test, 0 hata. Cihazda ek kontrol: not yazılırken/hata sonrası tabletı döndür, metin kalıyor mu.
- **Açık sorular:** Codex 45002c6 #2 (Activity yeniden oluşunca süren yazım sahipsiz kalır): probe için orkestratör reddetti; üründe yazıcı Activity ömründen bağımsız (servis) olacak — Aşama 1 kuralı. (#1 düzeltildi: sonuç görünümü `isSaveEnabled = false`, yalnız dört giriş alanı durum saklar.)
