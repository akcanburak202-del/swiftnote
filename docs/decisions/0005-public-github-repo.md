# 0005 — Kod public GitHub deposunda, CI ile

- **Durum:** kabul (kullanıcı, 2026-10-14)
- **Tarih:** 2026-10-14

## Bağlam

T-001 GitHub deposu açılıp açılmayacağını soruyordu. MateBridge public bir depoda (`akcanburak202-del/matebridge`) ve CI ile çalışıyor.

## Karar

- Depo: `akcanburak202-del/swiftnote`, **public**.
- `.github/workflows/check.yml`, `scripts/check.sh`'nin cihazsız kısmını çalıştırır; ilk hafta bilgi amaçlı, sonra zorunlu kapı.

## Sonuçlar

- Public depoda Actions dakikası sınırsız; private'ta aylık kota olurdu.
- Depodaki her şey herkese açık: notlar depoya hiç girmez (cihazda MateBridge klasörüne yazılır, ADR 0004). `.gitignore` log, `*.jsonl`, probe çıktısı ve imza anahtarlarını dışarıda tutar.
- `docs/NOTES.md` ve `docs/PROGRESS.md` public: buraya not metni, kişisel veri, IP adresi, token yazılmaz (AGENTS.md kural 3'ün belgelere uzantısı).
- Orkestratör her push'tan önce staged içeriği bu açıdan gözden geçirir.
