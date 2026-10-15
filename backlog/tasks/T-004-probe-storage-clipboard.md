---
id: T-004
title: "Probe C: dosya yazma ve pano"
status: ready
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

_(Ajan doldurur.)_

## Devir

- **Commit:**
- **Dokunulan dosyalar:**
- **Verify çıktısı (son satırlar):**
- **Varsayımlar:**
- **Test edilmeyenler / cihazda doğrulanacaklar:**
- **Açık sorular:**
