---
id: T-007
title: "Aşama 1 MVP: kısayol → durdur → not kutusu → kaydet + devam (tek uygulama)"
status: in-progress
phase: 1
owner: android-dev
depends_on: [T-001, T-002, T-003, T-004]
decisions: [0002, 0004]
files:
  - app/
verify: ./scripts/check.sh
---

## Amaç

Kullanıcının hemen kullanabileceği tek temiz uygulama (`dev.swiftnote`). Akış ve dosya biçimi: `docs/SPEC.md` "Akış" ve "Dosya biçimi". Kullanıcı hızlı ilerlemek istiyor: tek kart, sorunlar kullanımda bulunup düzeltilecek.

**Probe kodu kullanılabilir.** `probes/keys-overlay`, `probes/media-session`, `probes/storage` cihazda büyük ölçüde doğrulandı ve Codex'ten geçti. JVM testli saf sınıfları (`ShortcutMatcher`, `ShortcutTracker`, `NoteFormat`, `TimeFormat`) testleriyle birlikte `app/` altına `dev.swiftnote` paketine taşı; servis mantığını (`ProbeService`: pencere haritası, `setFilter`, `failOpen`, `ShortcutTracker` kullanımı) temel al. Probe'lara dokunma.

## Cihaz bulguları (`docs/NOTES.md`, 2026-10-15)

- Ctrl+Shift+N YouTube'da servise ulaşıyor (`meta=0x203041`, Num Lock `0x200000`), yol A (`TYPE_ACCESSIBILITY_OVERLAY`) kutuyu açıyor. **Yol A kullanılır**; B/C yok.
- Kutuda Enter ve Shift+Enter aynı davranıyor, Enter kaydetme yoluna hiç ulaşmıyor: IME (Huawei Celia) donanım Enter'ını `EditText` içinde işliyor. → `EditText` alt sınıfında `dispatchKeyEventPreIme` ile Enter'ı IME'den **önce** yakala: Shift yok → kaydet; Shift var → imlece `\n` ekle. Esc/BACK aynı yerde.
- Filtre açıkken YouTube'da üç parmak kaydırma çalışmıyor (bilinen kısıt, bu kartta çözülmez).
- Huawei logcat'te `Log.i` görünmüyor; hata logları `Log.w`/`Log.e`.

## Kabul kriterleri

**Kısayol ve filtre** (ADR 0002, `AGENTS.md` kural 1, 3)
- [ ] Erişilebilirlik servisi; filtre yalnız YouTube (`com.google.android.youtube`) penceresi girdi odağındayken açık, başka her yerde kapalı. Probe'daki tasarım: `TYPE_WINDOW_STATE_CHANGED` ile `windowId → paket` haritası, tuş yolunda yalnız `getWindows()`; `getRoot()` yok.
- [ ] Yalnız tam Ctrl+Shift+N DOWN/UP çifti tüketilir (repeatCount 0'da başlar, kutu UP'ta açılır); her şey `false`. Her istisna → `false` + filtre kapalı (fail open); kapatma başarısızsa `disableSelf()`.
- [ ] Tuş kodları hiçbir yere yazılmaz (logcat dahil). Not metni hiçbir yerde loglanmaz.

**Medya** (bildirim dinleyici; T-003 probe'u)
- [ ] `NotificationListenerService` + `MediaSessionManager.getActiveSessions` ile YouTube oturumu: başlık, kanal (`METADATA_KEY_ARTIST`, yoksa `ALBUM_ARTIST`/`AUTHOR`), konum (`position + (now - lastPositionUpdateTime) * speed` oynuyorsa).
- [ ] Kısayolda: oynuyorsa `pause()`, "biz durdurduk" hatırlanır. Kapanışta (kayıt, Esc, BACK, zaman aşımı) yalnız biz durdurduysak ve YouTube hâlâ odaktaysa `play()`. Medya komutu yalnız o anda YouTube girdi odağındayken gönderilir; `dispatchMediaKeyEvent` yok.
- [ ] Oturum yoksa ya da bildirim erişimi yoksa kutu yine açılır; not `Başlıksız video.md` dosyasına zaman damgasız yazılır.

**Not kutusu** (yol A)
- [ ] Ekranın altında ince kutu, videonun ortasını kapatmaz; üstünde salt okunur bağlam satırı `▶ <Başlık> · <Kanal> @ 12:34`. Açılınca imleç içinde, tıklamadan yazılabilir (IME'yi gerekiyorsa açıkça iste). `defaultFocusHighlightEnabled=false`.
- [ ] Enter (Shift yok, `dispatchKeyEventPreIme`) → boş değilse kaydet; boşsa kapat + devam. Shift+Enter → yeni satır. Esc/BACK → kaydetmeden kapat, boş değilse taslak. 2 dakika hiç tuş/dokunma/metin değişimi yoksa taslakla kapanır.
- [ ] Taslak bir sonraki kısayolda kutuya geri gelir (taslak hangi videoya ait olursa olsun metin geri gelir).
- [ ] Kutu açıkken odak YouTube'dan başka bir uygulamaya geçerse (doğrulanmış odak değişimi) kutu taslakla kapanır.

**Kayıt** (ADR 0004, `AGENTS.md` kural 2; T-004 probe'u)
- [ ] `/sdcard/MateBridge/Wi-Fi/Video Notları/<Başlık> — <Kanal>.md`, SPEC'teki biçim (frontmatter + başlık ilk yazımda, sonra yalnız ekleme, `fd.sync()`); dosya adı ≤ 255 UTF-8 bayt; çok satırlı not: sonraki satırlar iki boşlukla girintili.
- [ ] Yazma servis tarafında tek iş parçacıklı executor'da; tuş callback'i diski beklemez. Başarı → panoya `12:34 — metin` (zaman yoksa yalnız metin) → kutu kapanır → devam. Hata → kutu açık, metin duruyor, kutuda kısa hata mesajı. Aynı not iki kez yazılmaz (Enter'a ikinci basış yazım sürerken yok sayılır).
- [ ] Taslak `SharedPreferences` (`commit()`), ana iş parçacığında değil; servis kapanırken taslak yazılamazsa `filesDir` altında yedek dosya.

**Kurulum ekranı** (`MainActivity`)
- [ ] Sürüm (`BuildInfo`), üç izin durumu ve her biri için ayar düğmesi: erişilebilirlik servisi, bildirim erişimi, tüm dosyalara erişim (`MANAGE_EXTERNAL_STORAGE`). Kısa kullanım: "YouTube'da Ctrl+Shift+N". Son kaydedilen dosyanın adı (metni değil). `onResume`'da yenilenir.

**Genel**
- [ ] Bağımlılık eklenmez. Saf mantık JVM testli (kısayol, dosya adı, not biçimi, zaman biçimi, konum hesabı).
- [ ] `./scripts/check.sh` geçiyor.

## Kapsam dışı

Paylaş → bağlantı ekleme, kısayolu değiştirme, üç parmak kaydırma sorunu, YouTube dışı oynatıcılar.

## Cihazda (orkestratör + kullanıcı)

Önce `dev.swiftnote.probe.keys` servisi kapatılır/kaldırılır (iki servis aynı kısayolu tüketir). Sonra SPEC "Kabul" listesi.

## Plan

1. Probe'lardan saf sınıfları `app/` altına taşı (ShortcutMatcher/Tracker, NoteFormat, NoteWriter) + testleri; NoteFormat zamansız not ve "Başlıksız video.md" için genişler. Yeni saf sınıflar: `MediaMath` (konum tahmini, kanal seçimi), `ComponentList` (ayar listesinde servis var mı).
2. `SwiftNoteService` (erişilebilirlik): probe'daki pencere haritası / setFilter / failOpen tasarımı; kutu yaşam döngüsü burada (pause, kaydet, devam, taslak). Disk işleri tek iş parçacıklı executor'da.
3. `MediaSource` + `MediaAccessListener` (bildirim erişimi, yalnız getActiveSessions için).
4. `NoteBox` (yol A) + `NoteEditText` (`dispatchKeyEventPreIme`: Enter/Shift+Enter/Esc/BACK). `DraftStore` (SharedPreferences commit + filesDir yedek, bellek önbelleği).
5. `MainActivity` kurulum ekranı; manifest, servis XML'i, izinler.
6. `./scripts/check.sh`.

## Devir

## Açık sorular
