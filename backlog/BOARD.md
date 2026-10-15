# Board

_Orkestratör günceller. Son güncelleme: 2026-10-15 (T-001 oturumu)._

## Aşama 0 — kurulum ve probe'lar

| Kart | Başlık | Durum | Sahip | Bağımlı |
|---|---|---|---|---|
| T-001 | Repo, araç zinciri ve doğrulama kapısını kur | done | orchestrator + android-dev | — |
| T-002 | Probe A: kısayol yakalama ve odaklı not kutusu | todo | android-dev | T-001 |
| T-003 | Probe B: YouTube medya oturumu ve paylaşım bağlantısı | cihaz testi bekliyor | android-dev | T-001 |
| T-004 | Probe C: dosya yazma ve pano | cihaz testi bekliyor | android-dev | T-001 |
| T-006 | install-apk.sh paket adını APK'dan okusun | cihaz testi bekliyor | android-dev | T-001 |
| T-005 | Aşama 0 değerlendirmesi: bulgular, kararlar, Aşama 1 kartları | todo | orchestrator + user | T-002, T-003, T-004 |

T-002, T-003, T-004 ve T-006 ayrı dosyalara dokunur → T-001'den sonra paralel çalışabilir. Cihaz testleri ise her zaman tek tek.

## Cihaz testi bekliyor

_(yok)_
