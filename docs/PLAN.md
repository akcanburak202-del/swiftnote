# SwiftNote — Plan

**Tarih:** 2026-10-14. Aşamalar sırayla; bir aşamanın "Bitti" ölçütü karşılanmadan sonrakine geçilmez.

## Aşama 0 — kurulum ve probe'lar (belirsizlikleri önce cihazda kapat)

Kağıt üzerinde her parça yapılabilir görünüyor (`docs/research/2026-10-14-feasibility.md`) ama üç kritik soruyu yalnız tablet cevaplar. Probe'lar atılabilir; ürüne kod taşınmaz, **bilgi** taşınır (NOTES + kararlar).

| Kart | Ne sınanıyor |
|---|---|
| T-001 | Repo, araç zinciri, `scripts/check.sh`, sürüm kimliği, güvenli kurulum betiği, SDK seviyesi |
| T-002 | Probe A: donanım kısayolu erişilebilirlik servisine ulaşıyor mu; odaklı kutu tam ekran YouTube üstünde klavyeyi alıyor mu; MateBridge'de tuşlar etkilenmiyor mu |
| T-003 | Probe B: YouTube medya oturumu başlık/kanal/konum veriyor mu, durdur/devam çalışıyor mu; Paylaş bağlantısı |
| T-004 | Probe C: `MateBridge/Wi-Fi/Video Notları/` altına Türkçe adlı dosyaya ekleme; panoya yazma |
| T-005 | Değerlendirme: bulgular NOTES'a, kararlar 0002/0004 kabul ya da değişiklik, Aşama 1 kartları |

**Güvenlik (T-002 için zorunlu):** Tablet Mac'in tek ekranı. Kısayol probe'u tuşları yutarsa kullanıcı Mac'e yazamaz. Bu yüzden:
- Probe servisi yalnız tam kısayolu tüketir, geri kalan her şeyi geçirir; YouTube ön planda değilken hiçbir şey yapmaz.
- Kapatma yolu her denemeden önce kullanıcıya söylenir: Ayarlar → Erişilebilirlik → servisi kapat (dokunarak), ya da Mac'ten
  `adb shell settings put secure enabled_accessibility_services ""` (tüm erişilebilirlik servislerini kapatır; kullanıcının başka servisi varsa önce `settings get` ile değeri kaydet).
- Son çare: Mac'e Parsec ile ulaşmak (MateBridge `docs/RECOVERY.md`).

**Bitti:** `docs/research/2026-10-14-feasibility.md` sonundaki 8 belirsizliğin her biri NOTES'ta geçti/kaldı olarak kayıtlı; overlay yolu (A/B/C) ve depolama yolu kararlaştırılmış; Aşama 1 kartları yazılmış.

## Aşama 1 — MVP (çekirdek akış)

Tek uygulama: erişilebilirlik servisi (kısayol + ön plan takibi) + bildirim dinleyici (medya oturumu erişimi için) + not kutusu + dosya/pano yazıcı + kurulum ekranı (izinlerin durumu ve ayarlara kısayollar).

Saf mantık JVM testli: kısayol eşleme, dosya adı temizleme, not biçimi, zaman biçimi.

**Bitti:** `docs/SPEC.md` "Kabul" listesinin 7 maddesi tablette geçti; kullanıcı bir hafta gerçek izlemede kullandı ve NOTES'a izlenimini yazdık.

## Aşama 2 — cilalama (kullanıcıyla önceliklendirilir)

Adaylar: Paylaş → URL ekleme; kısayolu ayarlardan seçme; kutuda bağlam satırını düzenleme; son notu geri alma; YouTube dışı oynatıcılar.

## İlk günden yapılacaklar (MateBridge dersleri)

Bunlar MateBridge'de sonradan, bedel ödenerek eklendi (`~/Desktop/matebridge/docs/reviews/2026-10-03`, ADR 0022, 0037):
- `scripts/check.sh` tek doğrulama kapısı; GitHub deposu açılırsa CI (GitHub Actions) ilk haftadan.
- Sürüm kimliği: git SHA + `versionCode` (= derleme dakikası) uygulamaya gömülür, kurulum ekranında ve log başında görünür. README'de "Son doğrulama" tablosu.
- Günlük kurulan APK debug değil (`daily` buildType: `initWith(debug)`, `isDebuggable=false`). Sürüm düşürme hatası uygulamayı kaldırarak çözülmez (izinler ve veriler gider).
- Deney bayrakları varsayılan kapalı ve listeli.
- Birleşen worktree'ler düzenli silinir; `gradle.properties`'te `org.gradle.daemon.idletimeout=300000`.
- Her oturum sonunda `docs/PROGRESS.md` devir notu.
