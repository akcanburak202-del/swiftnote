# SwiftNote — Çalışma düzeni

**Tarih:** 2026-10-14. MateBridge'in `docs/WORKFLOW.md` düzeninden uyarlandı; değişiklikler o projenin derslerine dayanıyor (`docs/PLAN.md` → "İlk günden yapılacaklar").

## Roller

| Rol | Kim | Sorumluluk |
|---|---|---|
| Ürün sahibi + test | Kullanıcı | Kararlar, tablette deneme ("elde nasıl hissettiriyor"), aşama onayı |
| Orkestratör | Ana Claude Code oturumu (Opus) | Kart yazma, kararlar, birleştirme, cihaz işleri, raporlama. Uygulama kodunu kendisi düzeltmez. |
| Uygulayıcı | `android-dev` alt ajanı (Sonnet) | Tek kart, kendi dalında/worktree'sinde |
| İç denetçi | `reviewer` alt ajanı (Sonnet) | Her kartın diff'ini kart ve AGENTS.md'ye göre salt okunur inceler |
| Bağımsız denetçi | Codex, salt okunur | Kritik parçalar: kısayol filtresi, kutu odağı, dosya yazma |

Kullanıcı alt ajanlı çalışmayı tercih ediyor (MateBridge'de de böyleydi).

## Kurallar

1. Kart yoksa kod yok. Kart sözleşmedir; alt ajana kart yolu verilir, kart prompt'ta tekrar edilmez.
2. Bir dosyaya aynı anda tek ajan dokunur. Paralel yazma yalnız `files:` alanları ayrık kartlarda, her ajan kendi worktree'sinde (pratikte 2–3).
3. Birleştirme kapısı: `verify` geçer (çıktı devirde alıntılı) → `reviewer` → orkestratör diff'i okur → kritikse Codex. Yerel `./scripts/check.sh` her zaman asıl kapıdır. GitHub deposu varsa (T-001'de kullanıcıya sorulur) CI ilk hafta bilgi amaçlı, sonra zorunlu; depo yoksa CI yok, yerel check.sh yeterli.
4. Cihaz testleri her zaman **tek tek**, orkestratör yürütür, kullanıcı dener. Her adım sonunda 3–5 maddelik "tablette şunu dene" listesi.
5. Birleşmiş ama cihazda denenmemiş kart `done` olmaz (BOARD'da "cihaz testi bekliyor").
6. Kararlar `docs/decisions/`, cihaz bulguları `docs/NOTES.md`; sohbette kalmaz.
7. Çıkarım ile ölçüm ayrılır: NOTES'a "doğrulandı" yalnız cihazda görülen şey için yazılır, gerisi "çıkarım".
8. Oturum sonunda `docs/PROGRESS.md` en üste devir notu.

## Codex

MateBridge'deki `scripts/codex-review.sh` bu projeye T-001'de uyarlanır. Kurallar aynı:
- `codex exec -s read-only -m gpt-6.1-sol -c model_reasoning_effort="medium" "<istek>" </dev/null` (stdin kapalı; açık kalınca bir kez 25 dk boşa gitti).
- Efor her çağrıda açıkça verilir, en az `medium`; kritik parçalarda `high`.
- `gpt-6-astra` pahalı; kullanıcı onayı olmadan kullanılmaz.
- MateBridge'de iç `reviewer` 6 kartta, Codex 135 kartta kullanıldı: Codex fiilen asıl denetçiydi. Burada da kritik kartlarda atlanmaz.

## Cihaz ve araçlar

- adb: `~/Library/Android/sdk/platform-tools/adb` (PATH'te değil). **`adb kill-server` çalıştırma**: MateBridge'in USB tünellerini düşürür.
- Kurulum yalnız güvenli betikle (T-001; MateBridge `scripts/install-apk.sh` kuralları: yalnız paket + resource-id ile dokun, mağaza ekranlarına asla dokunma, HUAWEI olmayan cihazı reddet, `-r -d`). Metne göre tıklayan bir betik bir kez AppGallery'den istenmeyen uygulamalar kurdu.
- Yeni paketin ilk kurulumunda AppGallery risk kontrolü çıkar; kullanıcı onaylar. Başarıyı `dumpsys package <pkg> | grep lastUpdateTime` ile doğrula.
- Huawei logcat tamponu küçük: `adb logcat -G 16M`. Ekran görüntüsü: `adb exec-out screencap -p`.
- Kullanıcı tableti MateBridge ile Mac ekranı olarak kullanırken test yapılır: YouTube'a geçiş ve dönüş kullanıcının elinde.

## Paralellik

- Okuyucu/araştırmacı ajanlar: serbest (5–8).
- Yazan ajanlar: ayrık `files:` + donmuş ortak arayüz; her biri ayrı worktree.
- Cihaz: her zaman bir.
- Birleşen worktree'leri düzenli temizle (MateBridge'de ~70 birikmişti).
