---
id: T-001
title: Repo, araç zinciri ve doğrulama kapısını kur
status: review
phase: 0
owner: orchestrator      # git/GitHub/cihaz adımları orkestratör; Gradle iskeleti android-dev'e verilebilir
depends_on: []
decisions: [0003]
files:
  - settings.gradle.kts
  - build.gradle.kts
  - gradle.properties
  - gradle/
  - gradlew
  - gradlew.bat
  - app/
  - scripts/
  - .github/
  - README.md
verify: ./scripts/check.sh
---

## Amaç

Bundan sonraki her kartın üstüne oturacağı zemini kurmak: git deposu, MateBridge ile aynı araç zinciri, tek doğrulama kapısı, sürüm kimliği ve tablete güvenli kurulum. MateBridge'de bunların çoğu sonradan ve bedelle eklendi.

## Kapsam dışı

- Uygulama özellikleri (kısayol, kutu, medya, dosya). `app/` yalnız boş bir başlangıç ekranı.
- Probe'lar (T-002…T-004).

## Kabul kriterleri

- [x] Kullanıcıya soruldu ve cevap NOTES/karara yazıldı: paket adı, GitHub deposu açılsın mı, `Ctrl+Shift+N` uygun mu.
- [x] `git init`, ilk commit bu planlama dosyalarıyla; `main` dalı.
- [x] Gradle projesi ADR 0003'e göre: AGP 9.4.1, Gradle 9.8.0 wrapper, compileSdk 37, minSdk/targetSdk 31, arm64-v8a, Compose yok; `gradle.properties`'te `org.gradle.daemon.idletimeout=300000`.
- [x] `daily` buildType (`initWith(debug)`, `isDebuggable=false`); `versionCode` = derleme dakikası; git SHA `BuildConfig`'te ve başlangıç ekranında görünüyor.
- [x] `scripts/check.sh`: `assembleDebug assembleDaily testDebugUnitTest` + belgelerdeki kritik iddiaların varlık kontrolü (ör. AGENTS.md'de adı geçen dosyalar var mı). Tek satır "ALL OK" ya da net hata.
- [x] `scripts/install-apk.sh`: MateBridge'deki betikten uyarlandı (yalnız paket + resource-id ile dokun, mağaza ekranlarına asla, HUAWEI olmayan cihazı reddet, `-r -d`, sonucu `dumpsys package` ile doğrula). `adb kill-server` yok.
- [x] `scripts/codex-review.sh`: MateBridge'den uyarlandı (salt okunur, efor açık, `</dev/null`).
- [x] GitHub deposu açıldıysa `.github/workflows/check.yml`: check.sh'nin cihazsız kısmı (ilk hafta bilgi amaçlı, sonra zorunlu). Açılmadıysa bu madde atlanır.
- [x] README'ye "Son doğrulama" tablosu (tarih, build SHA, HarmonyOS sürümü, check.sh).

## Cihazda (orkestratör + kullanıcı)

- [x] `adb shell getprop ro.build.version.sdk` → 31 ya da 32 (NOTES'a). ≥33 ise ADR 0002'ye not düş (Android 13 kısıtlı ayarlar).
- [ ] Boş uygulama güvenli betikle kuruldu; AppGallery kontrolünü kullanıcı onayladı; ekranda SHA görünüyor.

## Plan

MateBridge `client-android` ayarları kopyalanıp küçültüldü (NDK/CMake/fixture kaldırıldı, paket `dev.swiftnote`). `check.sh` iki bileşen: `docs` (AGENTS/CLAUDE/README içindeki backtick yolları var mı) ve `android` (assembleDebug assembleDaily testDebugUnitTest). Betikler MateBridge'den uyarlandı. CI yalnız Linux işi, `continue-on-error` (ilk hafta).

## Devir

- **Commit:** c53deac (dal task/T-001-repo-toolchain-check)
- **Dokunulan dosyalar:** settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/wrapper/*, gradlew(.bat), app/ (build.gradle.kts, manifest, MainActivity, BuildInfo + test), scripts/{check,install-apk,codex-review}.sh, .github/workflows/check.yml, README.md (Son doğrulama tablosu)
- **Verify çıktısı (son satırlar):** `==> doc claims / OK: doc claims / ==> gradle / OK: gradle / check.sh: ALL OK`
- **Varsayımlar:** Gradle wrapper (9.8.0) ve build.gradle.kts MateBridge'den birebir; AGP 9.4.1 + compileSdk 37 derlendi. CI SDK paketleri `platforms;android-37.0`, `build-tools;36.0.0` (MateBridge ile aynı), CI henüz çalıştırılmadı. README tablosundaki SHA birleştirmede doldurulmalı.
- **Test edilmeyenler / cihazda doğrulanacaklar:** install-apk.sh çalıştırılmadı (adb yok). Cihazda: `./scripts/check.sh` sonrası `scripts/install-apk.sh`; AppGallery onayı; ana ekranda "build <sha>" ve UTC zamanı görünmeli; `dumpsys package dev.swiftnote | grep lastUpdateTime`; `getprop ro.build.version.sdk`. CI (check.yml) ilk push'ta yeşil mi bakılmalı.
- **Açık sorular:**
- **Orkestratör (2026-10-14):** reviewer engelleyici bulgu yok; orkestratör check.sh'yi yeniden çalıştırdı (ALL OK); `main`'e birleşti (89d5ae3). Küçük notlar sonraki bir karta: `.check-doc-fail.tmp` `.gitignore`'da değil; `versionCode = 1` yorumu yanıltıcı; manifestteki `<profileable>` kart dışı ama zararsız; install betiği `/sdcard/sn-ui.xml`'i cihazda bırakıyor (MateBridge'den miras).

### Devir eki: Codex bulguları (install-apk.sh)

- P1 adb: PATH'e hiç düşülmez; `$ADB`, yoksa `${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb`; yoksa net hata.
- P1 uiautomator dökümü: dump + cat + rm tek cihaz komutunda (`adb exec-out`), dosya anlık yaşar; çıkış tuzağı da `rm -f` yapar. `/dev/tty` ile dosyasız akış denenmedi (API 31'de test edilemedi), bu yüzden dosya-ve-sil yolu korundu.
- P2 `trap cleanup EXIT` (INT/TERM exit 130/143 ile EXIT'e düşer): yalnız başlatılan adb install istemcisini kill+wait, yerel log'u siler, cihazdaki dökümü siler.
- Cihazda doğrulanacak: kurulum sonrası `adb shell ls /sdcard/sn-ui.xml` "No such file" vermeli; install-apk.sh düğmeleri hâlâ buluyor mu (exec-out tek komut biçimi).
