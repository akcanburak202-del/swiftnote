# SwiftNote — Spec

**Tarih:** 2026-10-14 · **Kaynak:** ideaverse vault'undaki planlama konuşması (kullanıcı + Echo).
Kullanıcının söylediği kararlar **(kullanıcı)**, Echo'nun önerileri **(öneri)** diye işaretli. Öneriler Aşama 0 bulgularıyla değişebilir.

## Neden

Kullanıcı tablette YouTube izlerken not almak istiyor. Bugün videoyu durdurması, ardından köşedeki yüzen not penceresine tıklaması, yazması, sonra videoya geri dönüp başlatması gerekiyor. Bu kesinti, not alma isteğini öldürüyor. **(kullanıcı)**

Notlar sonra Mac'teki Obsidian vault'una (ideaverse) gider ve orada Echo'yla konuşulur. **(kullanıcı)**

## Ortam

- Huawei MatePad Pro 12.2 (2025), HarmonyOS 4.3 (Android 12 / API 31 uyumlu, GMS yok), YouTube uygulaması.
- Huawei klavyeli kılıf takılı; tablet laptop gibi kullanılıyor. **(kullanıcı)**
- Tablet aynı zamanda Mac mini'nin tek ekranı: MateBridge (`~/Desktop/matebridge`). Tabletteki `/sdcard/MateBridge/Wi-Fi/` klasörü Mac'te Finder'da görünür. **(kullanıcı)**

## Akış

1. YouTube oynarken (tam ekran dahil) **`Ctrl+Shift+N`** → video durur; ekranın altında ince bir not kutusu açılır, imleç içinde, tıklamaya gerek yok. **(kısayol ve tuş: kullanıcı, 2026-10-14)**
2. Kutunun üstünde salt okunur bir bağlam satırı: `▶ <Video başlığı> · <Kanal> @ 12:34`. **(öneri)**
3. **Enter** → not dosyaya yazılır **ve** panoya kopyalanır; kutu kapanır; video kaldığı yerden devam eder. **(dosya + pano: kullanıcı onayı)**
4. **Shift+Enter** → notta yeni satır. **Esc** → kaydetmeden kapat, video devam; kutuda yazı varsa **taslak** olarak saklanır ve bir sonraki kısayolda geri gelir (not asla kaybolmaz). **(öneri, Echo'nun kararı)**
5. Kutu videonun ortasını kapatmaz; durulan kare görünür kalır. **(öneri)**
6. İsteğe bağlı: YouTube'da Paylaş → SwiftNote → videonun bağlantısı o videonun dosyasına eklenir (YouTube medya oturumu URL vermiyor). **(öneri, Aşama 2)**

Öncelik: **not alırken pürüzsüz akış (kullanıcı).** Elin klavyeden kalkmaması, döngünün üç tuş olması.

## Dosya biçimi (öneri)

- Konum: `/sdcard/MateBridge/Wi-Fi/Video Notları/` — Mac'te MateBridge Wi-Fi klasörüyle görünür. **Vault'a otomatik taşıma yok; kullanıcı Finder'dan elle sürükler (kullanıcı).** Vault'taki hedef `+` klasörü (gelen kutusu).
- **Video başına bir dosya:** `<Başlık> — <Kanal>.md`. Dosya adında `/ \ : * ? " < > | # ^ [ ]` temizlenir, Türkçe karakterler korunur (UTF-8). Aynı video = aynı başlık + kanal.
- İçerik:

```markdown
---
kaynak: youtube
kanal: <Kanal>
olusturma: 2026-10-14
---
# <Başlık>

- **12:34** İlk not
- **18:02** İkinci not
  Shift+Enter ile gelen ikinci satır
```

- Her not dosyanın sonuna eklenir; mevcut içerik asla yeniden yazılmaz. Paylaşımla gelen bağlantı da gövdeye eklenir (`- 🔗 https://youtu.be/…`), frontmatter'a yazılmaz. **(Echo'nun kararı)**
- Panoya giden: yalnız not metni + zaman, ör. `12:34 — İlk not`.

## Hedefler

- Kısayoldan yazmaya geçiş anında (hedef: algılanan gecikme yok, < 300 ms).
- YouTube dışında SwiftNote'un tuşlara eklediği gecikme: **sıfır** (filtre kapalı).
- Kayıt + devam tek Enter.
- Yeniden başlatma, uygulamanın arka planda öldürülmesi ve güncellemeden sonra çalışmaya devam etmek.

## Hedef dışı

- Tam bir not uygulaması (not listesi, arama, düzenleme ekranı).
- YouTube dışındaki oynatıcılar (ileride düşünülebilir; mimari engellememeli).
- Vault'a otomatik senkron, bulut, hesap.
- Play Store yayını; tek cihaz, sideload.

## Anti-hedefler (çalışsa bile başarısızlık sayılır)

- **MateBridge ile Mac kullanırken tek bir tuşun bile yutulması, gecikmesi ya da değişmesi.** Bu yüzden tuş filtresi yalnız YouTube penceresi girdi odağındayken açık, başka her yerde kapalı; hata halinde filtre kapanır ve tuş geçer.
- Mac'e yazılan tuşların (parolalar dahil) herhangi bir yerde loglanması.
- Yazılan bir notun kaybolması.
- Not kutusunun ekranda takılı kalıp kapanamaması.
- Kalemle çalışırken Huawei HiWrite'ın görünür bir metin kutusu yüzünden devreye girmesi (kutu yalnız kısayolla açık olmalı).

## Kabul (Aşama 1 sonunda, kullanıcı tablette)

1. YouTube tam ekranken kısayol → video durur, kutu açık, imleç içinde, hemen yazılabiliyor.
2. Enter → video devam ediyor; dosyada doğru başlık, kanal ve zamanla not var; pano aynı metni içeriyor.
3. Shift+Enter yeni satır; Esc kaydetmeden kapatıyor, video devam ediyor, yazı taslak olarak sonraki kısayolda geri geliyor.
4. Aynı videoda üç not → tek dosyada sırayla üç madde.
5. MateBridge ön plandayken kısayol ve diğer tüm tuşlar Mac'e aynen ulaşıyor.
6. Tablet yeniden başlatıldıktan sonra kısayol hâlâ çalışıyor.
7. Dosya Mac'te Finder'da (MateBridge Wi-Fi) görünüyor ve Obsidian'da düzgün açılıyor.
