# PROGRESS — oturum devir notları

En yeni en üstte. Her oturum sonunda: ne yapıldı, durum, sıradaki adım, açık sorular.

## 2026-10-14 — planlama oturumu (ideaverse vault, Echo)

- **Yapıldı:** Fikir konuşuldu, kararlar alındı (ADR 0001–0004), spec, plan, çalışma düzeni, alt ajan tanımları ve Aşama 0 kartları (T-001…T-005) yazıldı. Fizibilite araştırması: `docs/research/2026-10-14-feasibility.md`. **Kod yok, git deposu yok.**
- **Durum:** Aşama 0 başlamadı.
- **Sıradaki adım:** Yeni Claude Code oturumu bu klasörde açılır → T-001 (git init, Gradle iskeleti, check.sh, güvenli kurulum betiği, SDK seviyesi kontrolü). Sonra T-002/T-003/T-004 paralel, cihaz testleri tek tek.
- **Kullanıcıya sorulacaklar (T-001):** paket adı (`dev.swiftnote` öneri); GitHub'da depo açılsın mı (MateBridge gibi); kısayol `Ctrl+Shift+N` uygun mu.

> Not (2026-10-14, planlama oturumu sonu): Taze bir ajan bu klasörü "inşa edecek kişi" gözüyle okudu; bulduğu boşluklar kapatıldı: YouTube dışında tuş kodu loglanmaz; tuş filtresi yalnız YouTube odaktayken açık; Esc/zaman aşımı yazıyı taslak olarak saklar; paylaşılan bağlantı gövdeye eklenir; CI kapısı GitHub deposu kararına bağlandı; hayatta kalma testleri T-002'ye eklendi. Bunlar Echo'nun kararları — kullanıcı itiraz ederse SPEC/ADR güncellenir.
