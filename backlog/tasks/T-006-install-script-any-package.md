---
id: T-006
title: install-apk.sh paket adını APK'dan okusun (probe'lar için)
status: todo
phase: 0
owner: android-dev
depends_on: [T-001]
decisions: [0003]
files:
  - scripts/install-apk.sh
verify: ./scripts/check.sh
---

## Amaç

Probe APK'ları (`dev.swiftnote.probe.keys`, `.media`, `.storage`) da tablete yalnız güvenli betikle kurulmalı. Bugün betikte `PKG="dev.swiftnote"` sabit: başka bir APK verilince kurulum doğrulaması (`lastUpdateTime`) yanlış pakete bakar ve sonunda yanlış uygulama açılır. Paket adı ve açılacak activity kurulan APK'dan okunmalı.

## Kapsam dışı

- Güvenlik kurallarında değişiklik: yalnız iki izinli (paket, resource-id) düğmesi, HUAWEI denetimi, `-r -d`, SDK adb'si, trap ve döküm temizliği aynen kalır (Codex düzeltmeleri, T-001 Devir).
- Probe'ların kendisi (T-002…T-004).

## Kabul kriterleri

- [ ] Paket adı APK'dan okunur (`$ANDROID_HOME/build-tools/<en yeni>/aapt2 dump badging` ya da `apkanalyzer`; yalnız SDK'da olan araç). Okunamazsa betik net hatayla durur, tahmin etmez.
- [ ] Başlatma: `am start -n <pkg>/<launchable-activity>` APK'dan okunan launchable activity ile; launchable activity yoksa başlatma atlanır ve bu yazdırılır.
- [ ] Varsayılan kullanım (`scripts/install-apk.sh`, `--debug`) eskisi gibi çalışır; çıktı paketi ismiyle yazar.
- [ ] Başlık yorumu güncel: kullanım satırına probe örneği (`scripts/install-apk.sh probes/storage/app/build/outputs/apk/debug/app-debug.apk` ya da probe'un gerçek çıktı yolu genel ifadeyle).
- [ ] `bash -n scripts/install-apk.sh` ve `verify` geçiyor. adb çalıştırılmaz.

## Cihazda (orkestratör + kullanıcı)

- [ ] İlk probe kurulumunda doğru paketin `lastUpdateTime`'ı değişiyor ve probe açılıyor.
- [ ] `scripts/install-apk.sh` (ürün) hâlâ `dev.swiftnote`'u kurup açıyor.

## Plan

_(Ajan kodlamadan önce doldurur: adımlar, dokunulacak dosyalar, riskler.)_

## Devir

_(Ajan bitirince doldurur.)_

- **Commit:**
- **Dokunulan dosyalar:**
- **Verify çıktısı (son satırlar):**
- **Varsayımlar:**
- **Test edilmeyenler / cihazda doğrulanacaklar:**
- **Açık sorular:**
