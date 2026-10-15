# Board

_Orkestratör günceller. Son güncelleme: 2026-10-15._

## Aşama 0 — kurulum ve probe'lar

| Kart | Başlık | Durum | Sahip | Bağımlı |
|---|---|---|---|---|
| T-001 | Repo, araç zinciri ve doğrulama kapısını kur | done | orchestrator + android-dev | — |
| T-002 | Probe A: kısayol yakalama ve odaklı not kutusu | done (kısmi; kalanlar T-007 cihaz testinde) | android-dev | T-001 |
| T-003 | Probe B: YouTube medya oturumu ve paylaşım bağlantısı | done (cihaz testi T-007'de) | android-dev | T-001 |
| T-004 | Probe C: dosya yazma ve pano | done (kısmi) | android-dev | T-001 |
| T-006 | install-apk.sh paket adını APK'dan okusun | done | android-dev | T-001 |
| T-005 | Aşama 0 değerlendirmesi: bulgular, kararlar, Aşama 1 kartları | done (kullanıcı hızlı geçişi onayladı) | orchestrator + user | T-002, T-003, T-004 |

T-002, T-003, T-004 ve T-006 ayrı dosyalara dokunur → T-001'den sonra paralel çalışabilir. Cihaz testleri ise her zaman tek tek.

## Aşama 1 — MVP

| Kart | Başlık | Durum | Sahip | Bağımlı |
|---|---|---|---|---|
| T-007 | MVP: kısayol → durdur → not kutusu → kaydet + devam | cihaz testi (0ad6f18 kurulu) | android-dev | T-001..T-004 |
| T-008 | Kısayolu Alt+N yap | in-progress | android-dev | T-007 |

## Cihaz testi bekliyor

_(yok)_
