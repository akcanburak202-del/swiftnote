# 0001 — Tam not uygulaması değil, YouTube üstünde yakalama kutusu

- **Durum:** kabul (kullanıcı, 2026-10-14)
- **Tarih:** 2026-10-14

## Bağlam

Kullanıcı YouTube izlerken not almak için videoyu durduruyor, yüzen not penceresine tıklıyor, yazıyor, videoya dönüp başlatıyor. İki seçenek konuşuldu: (a) kendi not uygulamasını yazıp uygulamalar arasında tek tuşla geçmek, (b) YouTube'dan hiç çıkmadan üstte açılan küçük bir yakalama kutusu.

## Karar

(b). SwiftNote bir not uygulaması değil, bir **yakalama aracı**: kısayol → durdur + kutu → Enter → kaydet + devam. Notların yaşadığı yer Obsidian vault'u; SwiftNote listeleme, arama, düzenleme yapmaz.

## Sonuçlar

- Uygulama geçişi yok; YouTube'un görev yığını bozulmaz (overlay seçeneğinde).
- Kutuda video başlığı ve zamanı otomatik gelir; not, ait olduğu anı kaybetmez.
- Daha az ekran ve kod; ama erişilebilirlik ve overlay gibi platform riskleri öne çıkar → Aşama 0 probe'ları.
