# SwiftNote — rules for every agent

SwiftNote is a personal Android app for one device: Huawei MatePad Pro 12.2 (2025), HarmonyOS 4.3
(Android 12 / API 31 compatible, no GMS), used with Huawei's keyboard folio. While YouTube plays, a
keyboard shortcut pauses the video and opens a small note box over it; Enter saves and resumes.
What and why: `docs/SPEC.md`. Phases: `docs/PLAN.md`. Process: `docs/WORKFLOW.md`.

## Hard rules

1. **Never break the tablet as a Mac terminal.** The tablet is the Mac mini's only screen
   (MateBridge, `~/Desktop/matebridge`). SwiftNote must never swallow, delay or remap a key outside
   its own shortcut, and must do nothing unless YouTube is the foreground app. Key filtering is
   **switched on only while a YouTube window has input focus** and off everywhere else (toggle the
   service's `flagRequestFilterKeyEvents` at runtime), so other apps get zero added latency. In
   split screen, "foreground" means the window with input focus. On any error, fail open: filtering
   off, key passed through, overlay closed (text kept as draft, rule 2), playback left as it was.
2. **Never lose a note.** Write to disk before closing the overlay or resuming the video. If the
   write fails, keep the box open with the text and show the error. Closing without saving (Esc,
   BACK, timeout, error) keeps non-empty text as a draft that reappears on the next shortcut.
3. **Never log what the user types.** Not note text, and outside YouTube not even key codes (they
   are the Mac's keystrokes over MateBridge, passwords included). Allowed: video titles, timings,
   counts, and modifier-only state.
4. **Do not touch MateBridge's adb setup.** Never run `adb kill-server` (it drops MateBridge's
   `adb reverse` tunnels). adb is at `~/Library/Android/sdk/platform-tools/adb`, not on PATH.
5. **Implementers never install to or drive the physical tablet.** Device work is the orchestrator's,
   one session at a time, with the user's go-ahead (see `docs/WORKFLOW.md`).

## Task cards

- You must have a task card in `backlog/tasks/`. **No card, no code.**
- Touch **only** the files and directories listed under `files:`. If you need more, stop and write
  the reason under *Açık sorular* in the card.
- If the card's *Plan* section is empty, write the plan first, commit it, then implement.
- Finish by filling *Devir* (handoff) honestly, including what was **not** tested on the device,
  and set `status: review`.

## Code

- Kotlin, plain Android Views (no Compose), minSdk/targetSdk 31, arm64-v8a. Toolchain as in
  MateBridge (`docs/decisions/0003-toolchain.md`).
- Do not add a dependency without a decision record. Test-time `junit:junit:4.13.2` is allowed.
- Pure logic (file naming, note formatting, time formatting, key matching) lives in plain Kotlin
  classes with JVM unit tests. Android glue stays thin.
- Language: code, identifiers, comments and commits in English; cards, plans and reports in Turkish.

## Git

- One task per branch: `task/T-XXX-slug`. Commits: `T-XXX: imperative summary`.
- Do not push, force-push, rewrite history or merge to `main`. Only the orchestrator merges.
- Reviewers never run `git checkout`, `git switch`, `git stash` or `git reset`; read other branches
  with `git show <branch>:<path>` or cd into the worktree.

## Where knowledge goes

- Device/platform findings: `docs/NOTES.md`, appended, dated, with build SHA and what was run.
- Decisions: `docs/decisions/`, never only in chat.
- Experiment flags: default off, listed with the card that will adopt or remove them.

## Verify

Until `scripts/check.sh` exists (T-001), "done" means `./gradlew assembleDebug testDebugUnitTest`
passes with the output quoted in the handoff. Never claim success on a failing build.
