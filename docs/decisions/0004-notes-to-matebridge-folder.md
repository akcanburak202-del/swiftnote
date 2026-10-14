# 0004 — Notlar `MateBridge/Wi-Fi` klasörüne ve panoya; otomatik taşıma yok

- **Durum:** konum ve pano kabul (kullanıcı, 2026-10-14); depolama API'si öneri, T-004'e bağlı
- **Tarih:** 2026-10-14

## Bağlam

Notlar Mac'teki ideaverse vault'una gidecek. MateBridge, tablette `/sdcard/MateBridge/Wi-Fi/` klasörünü Mac'te Finder'da gösteriyor (MateBridge ADR 0035; Mac menüsünde "Tablet dosyalarını aç" gerekir). Kullanıcı kopyala-yapıştır esnekliği de istedi; vault'a otomatik taşıma istemedi.

## Karar

- Dosya: `/sdcard/MateBridge/Wi-Fi/Video Notları/<Başlık> — <Kanal>.md`, video başına bir dosya, sona ekleme (biçim `docs/SPEC.md`).
- Her not aynı anda panoya da yazılır (MateBridge panoyu Mac'le paylaşıyor).
- Vault'a taşıma kullanıcının elinde (Finder'dan sürükle, vault'ta `+` klasörü).
- Depolama: kişisel sideload uygulama olduğu için **`MANAGE_EXTERNAL_STORAGE`** + düz `java.io` ekleme (SAF ağaç izni yedek). Mağaza politikası söz konusu değil.

## Sonuçlar

- MateBridge kapalıyken de notlar tablette birikir; hiçbir şey kaybolmaz.
- Dosya adı = video kimliği; aynı başlık ve kanalla iki farklı video tek dosyada birleşebilir (nadir, kabul).
