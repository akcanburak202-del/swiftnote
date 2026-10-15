---
id: T-005
title: "Aşama 0 değerlendirmesi: bulgular, kararlar, Aşama 1 kartları"
status: done
phase: 0
owner: orchestrator
depends_on: [T-002, T-003, T-004]
decisions: [0002, 0004]
files:
  - docs/
  - backlog/
verify: "-"
---

## Amaç

Probe'lardan çıkan bilgiyi kalıcı hale getirmek ve Aşama 1'i (MVP) kartlara bölmek.

## Kabul kriterleri

- [ ] `docs/research/2026-10-14-feasibility.md` sonundaki 8 belirsizliğin her biri NOTES'ta geçti/kaldı (5 numara "hayatta kalma": yeniden başlatma, bir gece bekleme, Son uygulamalardan kaydırma, `install -r` — T-002 probe'u üzerinde).
- [ ] ADR 0002 (overlay yolu A/B/C ve kısayol) ve 0004 (depolama API'si) "kabul" ya da yeni kararla değişti.
- [ ] Probe'larda çalışmayan bir şey varsa SPEC'teki akış kullanıcıyla yeniden konuşuldu.
- [ ] Aşama 1 kartları yazıldı: her biri tek bağlam penceresine sığar, `files:` alanları paralellik için ayrık; saf mantık ayrı kartta (JVM testli).
- [ ] Kullanıcı Aşama 1'e geçişi onayladı.

## Aşama 1'e taşınan kurallar (probe incelemelerinden)

Codex'in probe'larda bulduğu, probe için reddedilen ama ürün için geçerli kurallar. Aşama 1 kartlarının kabul kriterlerine girer.

- **Medya komutu yalnız YouTube girdi odağındayken** gönderilir (gecikmeli komut çalışma anında odağı yeniden doğrular); hedefi belirsiz `dispatchMediaKeyEvent` kullanılmaz (T-003, Codex #1/#2).
- **Not yazıcı Activity ömründen bağımsız** (servis tarafında, sıralı tek iş parçacığı); sonuç güncel kutuya bağlanır, aynı not iki kez yazılmaz (T-004, Codex 45002c6 #2).
- **Servis kapanırken taslak yazılamazsa** yedek konuma yazılır; metin kaybolmadan kutu kaldırılmaz (T-002, Codex edbb150 #1).
- **Tuş yolunda uygulama UI iş parçacığına senkron sorgu yok** (`getRoot()` vb.); odak `windowId → paket` önbelleği + `getWindows()` ile (T-002, Codex edbb150 #2).
- Dosya adı tamamı ≤ 255 UTF-8 bayt (T-004).
- **`getWindows()` önbellek yarışı:** odak değişiminden hemen sonraki tuş eski odağa göre değerlendirilebilir; YouTube dışındaki tuş kodu hiçbir yere yazılmaz, kısayol tüketimi bu yarışa göre değerlendirilir (T-002, Codex 703e7b3 #1; cihaz ölçümü).
- **Kutu kapanışı doğrulanmış odak değişimine dayanır**, başka paketin pencere olayına değil (T-002, Codex afdd63d #3).
- **Filtre her zaman diskten önce kapanır;** tuş callback'inde disk beklenmez (T-002, Codex afdd63d #1).
