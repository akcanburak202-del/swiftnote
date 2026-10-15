---
id: T-003
title: "Probe B: YouTube medya oturumu ve paylaşım bağlantısı"
status: in-progress
phase: 0
owner: android-dev
depends_on: [T-001]
decisions: [0001]
files:
  - probes/media-session/
verify: ./gradlew -p probes/media-session assembleDebug
---

## Amaç

YouTube'un medya oturumu üzerinden videoyu durdurup başlatabildiğimizi ve not kutusuna başlık, kanal ve zamanı koyabildiğimizi cihazda doğrulamak. Araştırma: `docs/research/2026-10-14-feasibility.md` §2.

## Kapsam dışı

- Kısayol ve overlay (T-002), dosya (T-004). Probe kendi ekranındaki düğmelerle çalışır.

## Kabul kriterleri

- [ ] Ayrı paket (`dev.swiftnote.probe.media`), `NotificationListenerService` (yalnız oturum erişimi için; bildirimleri okumaz/loglamaz).
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

1. `probes/media-session/` ayrı Gradle projesi (AGP 9.4.1, compileSdk 37, minSdk/targetSdk 31, arm64-v8a), paket `dev.swiftnote.probe.media`.
2. `ProbeListener` (NotificationListenerService, hiçbir bildirimi okumaz) yalnız `getActiveSessions` için ComponentName sağlar.
3. `MainActivity`: kodla kurulan Views; oturum listesi + YouTube metadata/playbackState dökümü, 3 sn gecikmeli durdur/devam, konumu oku (mm:ss), AudioManager yedek düğmeleri, bildirim erişimi ayar düğmesi.
4. `ShareActivity` (ACTION_SEND text/plain): EXTRA_TEXT ve `VideoId` ile çözülen kimliği gösterir. `VideoId`/`TimeFormat` saf Kotlin + JVM testleri.
5. Not metni loglanmaz; bildirimler okunmaz.

## Devir

- **Commit:**
- **Dokunulan dosyalar:**
- **Verify çıktısı (son satırlar):**
- **Varsayımlar:**
- **Test edilmeyenler / cihazda doğrulanacaklar:**
- **Açık sorular:**
