---
id: T-002
title: "Probe A: kısayol yakalama ve odaklı not kutusu"
status: todo
phase: 0
owner: android-dev
depends_on: [T-001]
decisions: [0002]
files:
  - probes/keys-overlay/
verify: ./gradlew -p probes/keys-overlay assembleDebug
---

## Amaç

Projenin en büyük iki belirsizliğini cihazda kapatacak bir araç üretmek: (1) Huawei klavyeli kılıftan gelen `Ctrl+Shift+N`, YouTube tam ekrandayken erişilebilirlik servisine ulaşıyor mu? (2) Bu kısayolla açılan bir kutu, tıklamadan klavye girdisini alıyor mu? Sonuç overlay yolunu (A/B/C) belirler. Araştırma: `docs/research/2026-10-14-feasibility.md` §1, §3.

## Kapsam dışı

- Medya oturumu, dosya, pano (T-003, T-004). Probe videoyu durdurmaz.
- Ürün kalitesi; bu kod atılacak.

## Kabul kriterleri

- [ ] Ayrı paket (`dev.swiftnote.probe.keys`), erişilebilirlik servisi `flagRequestFilterKeyEvents` ile.
- [ ] Tuş filtresi (`flagRequestFilterKeyEvents`) çalışma anında açılıp kapanır: yalnız YouTube penceresi girdi odağındayken açık (pencere olayları + `getWindows()` ile odak). Bölünmüş ekranda odak belirleyicidir. Geçişler loglanır (zaman damgalı).
- [ ] Filtre açıkken yalnız tam `Ctrl+Shift+N` down/up çifti tüketilir; diğer her şey `false`. Her istisna yakalanır → `false` + filtre kapanır.
- [ ] Log: YouTube içinde keyCode, action, metaState, repeatCount; **YouTube dışında hiçbir keyCode loglanmaz** (yalnız olay sayısı ve filtre durumu). Yazılan metin hiçbir yerde loglanmaz.
- [ ] Kısayolla kutu açılır. Üç yol bir ayar anahtarıyla seçilebilir: A `TYPE_ACCESSIBILITY_OVERLAY`, B `TYPE_APPLICATION_OVERLAY`, C saydam Activity. Hiçbirinde `FLAG_NOT_FOCUSABLE` yok.
- [ ] Kutuda Enter → kapat, Shift+Enter → yeni satır, Esc → kapat (BACK de yakalanır). Esc/BACK ile kapanırken yazı varsa taslak olarak saklanır ve sonraki kısayolda geri gelir. Kutu 60 sn boyunca **hiç tuş ya da dokunma olmazsa** kendiliğinden kapanır (yazmak süreyi sıfırlar; takılı kalma emniyeti), taslak yine saklanır.
- [ ] Probe uygulamasının ana ekranında: servis ve filtre durumu, seçili yol, YouTube içindeki son 20 tuş olayı, "servisi kapat" düğmesi.
- [ ] `verify` geçiyor.

## Cihazda (orkestratör + kullanıcı)

Önce kullanıcıya kapatma yolu söylenir (`docs/PLAN.md` Aşama 0 güvenlik).

- [ ] Huawei ayarlarındaki klavye kısayolları listesine bakıldı; `Ctrl+Shift+N` çakışmıyor.
- [ ] YouTube tam ekran: kısayol servise ulaşıyor, YouTube tepki vermiyor.
- [ ] A, B, C ayrı ayrı: tıklamadan yazılabiliyor mu, imleç görünüyor mu, YouTube tam ekran kalıyor mu, Enter/Shift+Enter/Esc çalışıyor mu. Hangi IME etkin, not edildi (SwiftKey/Huawei).
- [ ] C seçeneğinde YouTube PiP'e geçiyor mu, ana akışa atıyor mu.
- [ ] **MateBridge öndeyken servis açık:** logda filtre kapalı görünüyor; Mac'te normal yazma, `Ctrl+Shift+N` dahil kısayollar ve tuş tekrarı aynen çalışıyor.
- [ ] YouTube ↔ MateBridge geçişlerinde ve bölünmüş ekranda filtre doğru zamanda açılıp kapanıyor (log).
- [ ] Hayatta kalma: yeniden başlatma, bir gece bekleme, Son uygulamalardan kaydırma, `adb install -r` sonrası servis açık ve kısayol çalışıyor (HarmonyOS pil "uygulama başlatma" ayarı elle yapıldıktan sonra).
- [ ] Kalem kullanırken HiWrite kutuya karışıyor mu (kutu kapalıyken hiçbir şey olmamalı).
- [ ] Sonuçlar NOTES'a, geçti/kaldı ile.

## Plan

_(Ajan doldurur.)_

## Devir

- **Commit:**
- **Dokunulan dosyalar:**
- **Verify çıktısı (son satırlar):**
- **Varsayımlar:**
- **Test edilmeyenler / cihazda doğrulanacaklar:**
- **Açık sorular:**
