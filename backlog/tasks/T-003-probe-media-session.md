---
id: T-003
title: "Probe B: YouTube medya oturumu ve paylaşım bağlantısı"
status: todo
phase: 0
owner: android-dev
depends_on: [T-001]
decisions: [0001]
files:
  - probes/media-session/
verify: (cd probes/media-session && ./gradlew assembleDebug)
---

## Amaç

YouTube'un medya oturumu üzerinden videoyu durdurup başlatabildiğimizi ve not kutusuna başlık, kanal ve zamanı koyabildiğimizi cihazda doğrulamak. Araştırma: `docs/research/2026-10-14-feasibility.md` §2.

## Kapsam dışı

- Kısayol ve overlay (T-002), dosya (T-004). Probe kendi ekranındaki düğmelerle çalışır.

## Kabul kriterleri

- [ ] Ayrı paket (`<paket>.probe.media`), `NotificationListenerService` (yalnız oturum erişimi için; bildirimleri okumaz/loglamaz).
- [ ] Ekranda: etkin oturumlar listesi; YouTube oturumu için TITLE, ARTIST, ALBUM, MEDIA_ID, DURATION, playbackState (durum, position, lastPositionUpdateTime, speed).
- [ ] Düğmeler: "3 sn sonra durdur", "3 sn sonra devam" (kullanıcı arada YouTube'a geçebilsin), "konumu oku". Durdurduktan sonra okunan konum ekranda `mm:ss`.
- [ ] Yedek düğme: `AudioManager.dispatchMediaKeyEvent` ile durdur/devam.
- [ ] Paylaş hedefi (`ACTION_SEND` text/plain): gelen EXTRA_TEXT'i ve çözümlenen video kimliğini gösterir.
- [ ] `verify` geçiyor.

## Cihazda (orkestratör + kullanıcı)

- [ ] `adb shell dumpsys media_session` YouTube oynarken alındı, NOTES'a (özet).
- [ ] TITLE = video başlığı, ARTIST = kanal mı? MEDIA_ID video kimliği içeriyor mu?
- [ ] Durdur/devam tam ekran YouTube'da çalışıyor; durdurduktan sonra okunan konum ekrandaki zamanla ±1 sn.
- [ ] Reklam oynarken oturum ne gösteriyor (not edilir).
- [ ] YouTube Paylaş menüsünde probe görünüyor; bağlantı çözümleniyor.

## Plan

_(Ajan doldurur.)_

## Devir

- **Commit:**
- **Dokunulan dosyalar:**
- **Verify çıktısı (son satırlar):**
- **Varsayımlar:**
- **Test edilmeyenler / cihazda doğrulanacaklar:**
- **Açık sorular:**
