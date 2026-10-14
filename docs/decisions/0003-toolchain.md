# 0003 — Araç zinciri MateBridge ile aynı

- **Durum:** kabul (paket adı: kullanıcı, 2026-10-14); sürümler T-001'de derlemeyle doğrulanır
- **Tarih:** 2026-10-14

## Bağlam

Aynı Mac, aynı tablet. MateBridge'in Android araç zinciri çalışıyor ve kurulum tuzakları (AppGallery kontrolü, adb, Gradle daemon belleği) çözülmüş durumda.

## Karar

- AGP 9.4.1 (yerleşik Kotlin), Gradle 9.8.0 wrapper; compileSdk 37, **minSdk 31, targetSdk 31** (sideload; lint `checkReleaseBuilds=false`); yalnız arm64-v8a.
- Düz Android View'ları (Compose yok). Çalışma zamanı bağımlılığı yok; test için `junit:junit:4.13.2`.
- JDK: Android Studio JBR. SDK: `~/Library/Android/sdk`. CI: Temurin 21.
- `daily` buildType: `initWith(debug)`, `isDebuggable=false`, debug imza anahtarı. `versionCode` = derleme dakikası; git SHA `BuildConfig`'e gömülür.
- Paket adı: `dev.swiftnote` (kullanıcı onayladı, 2026-10-14).

## Sonuçlar

- MateBridge'den kopyalanabilecek şeyler: `client-android` Gradle ayarları, `scripts/install-apk.sh`, `scripts/codex-review.sh`, `.github/workflows/check.yml` yapısı. Kopya, bu projenin ihtiyacına göre küçültülür.
- MateBridge'in `client-android/AGENTS.md`'si var olmayan bir sürüm kataloğu dosyasından bahsediyordu: kopyalarken belgeyi kodla karşılaştır.
