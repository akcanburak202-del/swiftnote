---
id: T-005
title: "Aşama 0 değerlendirmesi: bulgular, kararlar, Aşama 1 kartları"
status: todo
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
