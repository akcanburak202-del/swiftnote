# Backlog

Her iş bir **görev kartıdır**: `tasks/T-XXX-kisa-ad.md`, şablon `templates/task.md`. Kart, orkestratörle ajan arasındaki sözleşmedir; ajan kartın dışına çıkmaz. (MateBridge'deki düzenin aynısı, Backlog.md yaklaşımına benzer: düz Markdown + YAML frontmatter.)

## Durumlar

`todo` → `in-progress` → `review` → `done`. Takılan iş `blocked`; neden kartın *Açık sorular* maddesine yazılır.

## Akış

1. Orkestratör kartı yazar: amaç, kapsam dışı, `files:`, kabul kriterleri, cihaz maddeleri, `verify`.
2. `android-dev` kartı alır, `in-progress` yapar; *Plan* boşsa önce planı yazar ve commit'ler.
3. Uygular, `verify` geçer, *Devir*i doldurur, `review` yapar.
4. `reviewer` inceler; kritik parçalarda Codex de (`docs/WORKFLOW.md`).
5. Orkestratör birleştirir; cihaz maddeleri varsa kullanıcıyla tablette dener, sonucu `docs/NOTES.md`'ye yazar.
6. Cihaz maddeleri de geçince kart `done` olur, `BOARD.md` güncellenir.

## Kurallar

- Kart tek bağlam penceresine sığacak boyutta olur; büyükse bölünür.
- Aynı dosyaya dokunan iki kart aynı anda `in-progress` olamaz.
- Birleşmiş ama cihazda denenmemiş kart `done` olmaz, `review`de kalır ve BOARD'da "cihaz testi bekliyor" listesinde görünür (MateBridge dersi: birleştirmeler cihaz testinin önüne geçmesin).
- `BOARD.md`'yi yalnız orkestratör günceller (ileride kartlardan üreten bir betik yazılabilir).
