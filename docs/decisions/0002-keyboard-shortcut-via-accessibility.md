# 0002 — Tetikleyici klavye kısayolu, erişilebilirlik servisiyle

- **Durum:** kabul (kısayol: kullanıcı 2026-10-14; mekanizma ve overlay yolu A `TYPE_ACCESSIBILITY_OVERLAY`: T-002 cihazda 2026-10-15)
- **Tarih:** 2026-10-14

## Bağlam

Kullanıcı tableti Huawei klavyeli kılıfla laptop gibi kullanıyor; tetikleyici klavyede olmalı. Başka bir uygulama (YouTube) öndeyken donanım tuşunu yakalamanın HarmonyOS 4.3 / API 31'de bilinen yolu `AccessibilityService.onKeyEvent` (`flagRequestFilterKeyEvents`). Android 15/16'nın kısayol API'leri bu cihazda yok ve zaten sistem uygulamalarına ait.

Bilinen cihaz gerçekleri (MateBridge NOTES): Cmd/Meta uygulamalara ulaşmıyor; Fn ve Huawei halka tuşunu sistem yutuyor; Esc hem ESCAPE hem BACK üretiyor; tuş tekrarı `repeatCount>0`.

## Karar

- Kısayol **`Alt+N`** (kullanıcı, 2026-10-15). İlk karar Ctrl+Shift+N idi (2026-10-14) ve cihazda çalıştı, ama YouTube'da Ctrl tek başına YouTube'un kendi kısayol panelini açıyor; N'yi biz tükettiğimiz için panel açık kalıyor ve oynatmayı bozuyor. Alt YouTube'da hiçbir şey tetiklemiyor (T-007 cihaz testi, `docs/NOTES.md`).
- Tuş filtresi (`flagRequestFilterKeyEvents`) çalışma anında açılıp kapanır: yalnız **YouTube penceresi girdi odağındayken** (`com.google.android.youtube`; bölünmüş ekranda odak belirleyici) açık, başka her yerde kapalı → MateBridge ve diğer uygulamalara sıfır ek gecikme.
- Filtre açıkken yalnız **tam kısayolun** down/up çifti tüketilir; diğer her tuş anında `false`. Hata → filtre kapanır, tuş geçer (fail open).
- YouTube dışında keyCode loglanmaz (Mac'e yazılan tuşlar, parolalar dahil).
- Kutu açıkken servis Enter/Shift+Enter/Esc dışında hiçbir şeye karışmaz.
- Yedekler (sırayla): hızlı ayarlar kutucuğu, yüzen düğme, erişilebilirlik düğmesi.

## Sonuçlar

- Filtre açıkken servis her tuşu önce görür; çalışma anında aç/kapa bunu YouTube'la sınırlar. Ön plan tespiti böylece güvenlik açısından kritik parça olur; T-002 geçişleri loglayarak sınar.
- HarmonyOS servisleri yeniden başlatmada veya bellek temizliğinde kapatabilir: pil "uygulama başlatma" ayarı elle, pil optimizasyonu kapalı, servis çökmez olmalı.
