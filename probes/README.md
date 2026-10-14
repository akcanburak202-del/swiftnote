# Probes

Her probe tek bir soruya cevap veren **atılabilir** küçük bir Android uygulamasıdır (MateBridge `probes/` düzeni). Ürüne kod taşınmaz; bulgu `docs/NOTES.md`'ye, karar `docs/decisions/`'a taşınır. Her probe'un kendi kartı, kendi paket adı ve kendi klasörü var → paralel geliştirilebilir.

| Probe | Soru | Kart |
|---|---|---|
| `keys-overlay/` | Kısayol erişilebilirlik servisine ulaşıyor mu, odaklı kutu tam ekran YouTube üstünde klavyeyi alıyor mu, MateBridge'de tuşlar etkilenmiyor mu? | T-002 |
| `media-session/` | YouTube medya oturumu başlık/kanal/konum veriyor mu, durdur/devam çalışıyor mu, Paylaş bağlantı veriyor mu? | T-003 |
| `storage/` | `MateBridge/Wi-Fi/Video Notları/` altına Türkçe adlı dosyaya ekleme ve panoya yazma çalışıyor mu? | T-004 |

Probe'lar ürünle aynı Gradle sürümlerini kullanır ama ayrı Gradle projeleridir (ya da kök projede ayrı modüller; T-001 karar verir).
