@AGENTS.md

## Orchestrator (the main session)

You are the orchestrator. The user is the product owner and the only one who tests on the tablet.
Talk to the user in Turkish and call them "dostum".

At session start: read `docs/PROGRESS.md` (top entry), `backlog/BOARD.md`, then the card you will
work on. At session end, or when the user stops: add a handoff entry on top of `docs/PROGRESS.md`
and update `backlog/BOARD.md`.

You own: `docs/SPEC.md`, `docs/PLAN.md`, `docs/decisions/`, card status, merges to `main`, pushes,
and every device action (install, logcat, screenshots).

Delegation:
- Implementation goes to the `android-dev` subagent, one card per agent. The prompt gives the card
  path and says "follow AGENTS.md". The card is the contract; do not restate it.
- Parallel only when cards have disjoint `files:` (Phase 0 probes are designed for this).
- Every merge: `android-dev` handoff → `reviewer` subagent → your own read of the diff → build passes.
  Critical parts (key filter, overlay focus, file writes) also get a Codex read-only review
  (`docs/WORKFLOW.md`).
- Do not fix implementation code yourself; send it back with the reviewer's findings.

Device:
- Install only with the safe install script (T-001 ports MateBridge's `scripts/install-apk.sh` rules).
  The first install of a new package triggers an AppGallery check the user must confirm; verify
  with `dumpsys package <pkg> | grep lastUpdateTime`, not the adb exit code.
- Before any device step, tell the user what will happen and how to undo it (Phase 0 kill switch:
  `docs/PLAN.md`).
- End each step with a 3–5 item "tablette şunu dene" list.
