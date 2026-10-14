---
id: T-001
title: Repo, araç zinciri ve doğrulama kapısını kur
status: todo
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
- [ ] Gradle projesi ADR 0003'e göre: AGP 9.4.1, Gradle 9.8.0 wrapper, compileSdk 37, minSdk/targetSdk 31, arm64-v8a, Compose yok; `gradle.properties`'te `org.gradle.daemon.idletimeout=300000`.
- [ ] `daily` buildType (`initWith(debug)`, `isDebuggable=false`); `versionCode` = derleme dakikası; git SHA `BuildConfig`'te ve başlangıç ekranında görünüyor.
- [ ] `scripts/check.sh`: `assembleDebug assembleDaily testDebugUnitTest` + belgelerdeki kritik iddiaların varlık kontrolü (ör. AGENTS.md'de adı geçen dosyalar var mı). Tek satır "ALL OK" ya da net hata.
- [ ] `scripts/install-apk.sh`: MateBridge'deki betikten uyarlandı (yalnız paket + resource-id ile dokun, mağaza ekranlarına asla, HUAWEI olmayan cihazı reddet, `-r -d`, sonucu `dumpsys package` ile doğrula). `adb kill-server` yok.
- [ ] `scripts/codex-review.sh`: MateBridge'den uyarlandı (salt okunur, efor açık, `</dev/null`).
- [ ] GitHub deposu açıldıysa `.github/workflows/check.yml`: check.sh'nin cihazsız kısmı (ilk hafta bilgi amaçlı, sonra zorunlu). Açılmadıysa bu madde atlanır.
- [ ] README'ye "Son doğrulama" tablosu (tarih, build SHA, HarmonyOS sürümü, check.sh).

## Cihazda (orkestratör + kullanıcı)

- [ ] `adb shell getprop ro.build.version.sdk` → 31 ya da 32 (NOTES'a). ≥33 ise ADR 0002'ye not düş (Android 13 kısıtlı ayarlar).
- [ ] Boş uygulama güvenli betikle kuruldu; AppGallery kontrolünü kullanıcı onayladı; ekranda SHA görünüyor.

## Plan

_(Doldurulacak.)_

## Devir

- **Commit:**
- **Dokunulan dosyalar:**
- **Verify çıktısı (son satırlar):**
- **Varsayımlar:**
- **Test edilmeyenler / cihazda doğrulanacaklar:**
- **Açık sorular:**
