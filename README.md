# SwiftNote

YouTube izlerken tablet klavyesinde tek kısayolla videoyu durdurup üstte açılan kutuya not al, Enter ile kaydet, video kaldığı yerden devam etsin. Huawei MatePad Pro 12.2 (2025, HarmonyOS 4.3) ve klavyeli kılıfı için kişisel, sideload edilen bir Android uygulaması.

Take a note over YouTube with one keyboard shortcut: the video pauses, a small input box appears with the video title and time, Enter saves and resumes.

> **Durum:** Aşama 0 (probe'lar) başlamadı. Henüz kod yok. Bu klasör 2026-10-14'te ideaverse vault'unda yapılan planlama oturumunda hazırlandı; kodlama ayrı bir Claude Code oturumunda yapılır.

## Yeni oturum buradan başlar

1. `CLAUDE.md` (otomatik yüklenir) → `AGENTS.md`
2. `docs/PROGRESS.md` — en son oturumun devir notu
3. `docs/SPEC.md` — ne yapıyoruz, neden, ne yapmıyoruz
4. `docs/PLAN.md` — aşamalar ve çıkış ölçütleri
5. `backlog/BOARD.md` — sıradaki kart

## Düzen

```text
AGENTS.md            every agent's rules (short)
CLAUDE.md            @AGENTS.md + orchestrator protocol
.claude/agents/      android-dev (implementer), reviewer (read-only)
docs/SPEC.md         what and why, flow, file format, anti-goals, acceptance
docs/PLAN.md         phases with exit criteria
docs/WORKFLOW.md     roles, merge gate, parallelism, device testing
docs/decisions/      ADRs (NNNN-slug.md), index in README.md
docs/NOTES.md        append-only dated device/platform findings
docs/PROGRESS.md     session handoff log, newest on top
docs/research/       dated research reports
backlog/             task cards (tasks/T-XXX-slug.md), template, BOARD.md
probes/              throwaway one-question experiments (Phase 0)
```

Kardeş proje ve tecrübe kaynağı: `~/Desktop/matebridge` (aynı tablet, aynı araç zinciri, aynı çalışma düzeni).

## Son doğrulama

| Tarih | Build SHA | HarmonyOS | `scripts/check.sh` |
|---|---|---|---|
| 2026-10-15 | `98ae409` | 4.3.0.145 (API 31), kuruldu ve açıldı | ALL OK (yerel + CI) |

Doğrulama: `./scripts/check.sh` (belge iddiaları + Gradle). Cihazsız kısmı CI'da da çalışır (`.github/workflows/check.yml`).
