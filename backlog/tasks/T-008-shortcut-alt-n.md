---
id: T-008
title: "Kısayolu Alt+N yap (Ctrl YouTube'un kısayol panelini açıyor)"
status: done
phase: 1
owner: android-dev
depends_on: [T-007]
decisions: [0002]
files:
  - app/
verify: ./scripts/check.sh
---

## Amaç

YouTube'da Ctrl'ye basmak YouTube'un kendi kısayol panelini açıyor (arka plan kararıyor). Ctrl+Shift+N'de N'yi biz tükettiğimiz için YouTube başka tuş görmüyor, panel açık kalıyor ve panel açıkken oynatma/kısayol düzgün çalışmıyor. Alt YouTube'da hiçbir şey tetiklemiyor (kullanıcı, cihazda, 2026-10-15). Kullanıcı kararı: kısayol **Alt+N** (ADR 0002 güncellendi).

## Kabul kriterleri

- [ ] `ShortcutMatcher`: tam Alt+N — Alt basılı (sol ya da sağ Alt, `META_ALT_ON`), Ctrl, Shift ve Meta basılı değil. Ctrl+Shift+N artık eşleşmez. JVM testleri güncel (Alt+N, sağ Alt+N, Alt+Shift+N hayır, Ctrl+Alt+N hayır, Ctrl+Shift+N hayır, Num Lock `0x200000` açıkken Alt+N evet).
- [ ] `ShortcutTracker` tüketim/aç mantığı aynı kalır (yalnız N'nin DOWN/UP çifti tüketilir; Alt geçer).
- [ ] Kullanıcıya görünen metinler (kurulum ekranı, servis açıklaması) ve kod yorumları Alt+N der.
- [ ] `./scripts/check.sh` geçiyor.

## Plan

Matcher: Alt şart, Ctrl/Shift/Meta yasak; testler ve metinler güncellenir.

## Devir

Dosyalar: ShortcutMatcher(+Test), ShortcutTrackerTest, strings.xml, MainActivity, SwiftNoteService (yorum). check.sh: ALL OK. Tablette denenmedi.
Tablette kontrol: YouTube'da Alt+N not kutusunu açıyor mu; Ctrl paneli açılmıyor mu; Ctrl+Shift+N artık bir şey yapmıyor mu; Alt tek başına ve başka Alt kombinasyonları geçiyor mu.
