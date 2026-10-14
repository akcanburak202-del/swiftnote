# SwiftNote feasibility (research, 2026-10-14)

Web research by a planning-session subagent, no device tests. **Everything here is inference until a Phase 0 probe confirms it on the tablet.** Device facts marked "MateBridge NOTES" were observed on this exact tablet.

**Verdict:** likely feasible as one sideloaded APK with four pieces: an AccessibilityService (shortcut, probably also the overlay), a NotificationListenerService (only to unlock media-session access), MANAGE_EXTERNAL_STORAGE, and SYSTEM_ALERT_WINDOW as a fallback.

## Platform baseline

HarmonyOS 4.3 maps to Android 12 (MateBridge NOTES: API 31, no GMS). Consequences: Android 13 "restricted settings" for sideloaded accessibility apps should not apply; Android 14/15 background-launch opt-ins do not apply; Android 15/16 key-gesture APIs do not exist (and are system-only). Sources: https://en.wikipedia.org/wiki/HarmonyOS_version_history, https://www.androidauthority.com/customize-keyboard-shortcuts-in-android-16-3527394/

## 1. Global shortcut while another app is in front

- `AccessibilityService.onKeyEvent(KeyEvent)`; service XML `android:accessibilityFlags="flagRequestFilterKeyEvents"`, `android:canRequestFilterKeyEvents="true"`; manifest service with `android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"` and the `android.accessibilityservice.AccessibilityService` intent filter.
- Sees hardware key events before the rest of the system; returning `true` consumes. Keep down/up pairs consistent: consume both down and up of N. Use `isCtrlPressed()/isShiftPressed()`.
- AOSP's accessibility input filter sits ahead of dispatch-time shortcut handling, so most Ctrl combos should arrive (reading of AOSP, not documented). Huawei's own keyboard shortcuts (40+, HarmonyOS key) may be captured earlier — check Settings → keyboard shortcuts and avoid them.
- While the overlay is open, `onKeyEvent` still sees every key first: return `false` for everything except your own handling, or typing breaks.
- Fallbacks: Quick Settings tile (`TileService`), floating bubble (TYPE_APPLICATION_OVERLAY), accessibility button.
- Sources: https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android12-release/core/java/android/accessibilityservice/AccessibilityService.java, https://consumer.huawei.com/sg/support/content/en-gb15823823

## 2. Pause/resume and metadata

- `MediaSessionManager.getActiveSessions(ComponentName(ctx, NotifListener::class.java))` → controller with `packageName == "com.google.android.youtube"` → `transportControls.pause()/play()`, `metadata` `METADATA_KEY_TITLE / ARTIST / MEDIA_ID`, `playbackState.position` (read after pause; else extrapolate with `lastPositionUpdateTime` and `playbackSpeed`).
- Needs a `NotificationListenerService` (`BIND_NOTIFICATION_LISTENER_SERVICE`) enabled by the user under Notification access. `MEDIA_CONTENT_CONTROL` is system-only.
- **What YouTube exposes is unverified.** Expected: TITLE = video title, ARTIST = channel, position kept current; URL/ID probably not exposed (check MEDIA_ID). Ground truth: `adb shell dumpsys media_session` while YouTube plays.
- Fallback: `AudioManager.dispatchMediaKeyEvent(KEYCODE_MEDIA_PAUSE/PLAY)` — no permission, no metadata, hits the last active session.
- URL: YouTube Share → SwiftNote (`ACTION_SEND` text/plain), EXTRA_TEXT `youtu.be/ID?si=…`; add `&t=<sec>s` from the position.
- Sources: https://developer.android.com/reference/android/media/session/MediaController, https://developer.android.com/media/legacy/mediasession

## 3. Overlay with keyboard focus over fullscreen video

- **A (preferred):** `TYPE_ACCESSIBILITY_OVERLAY` via the service's WindowManager — no SYSTEM_ALERT_WINDOW, stacks above fullscreen apps. Do **not** set `FLAG_NOT_FOCUSABLE` (it blocks IME and key input); use `FLAG_NOT_TOUCH_MODAL`, `SOFT_INPUT_STATE_ALWAYS_VISIBLE`, `requestFocus()` after attach. Open question: whether an a11y overlay becomes the key-focus target on this ROM (forum reports of failures are common).
- **B:** `TYPE_APPLICATION_OVERLAY` + SYSTEM_ALERT_WINDOW — the classic floating-notes pattern, same focus rules.
- **C:** transparent Activity (`Theme.Translucent.NoTitleBar`, `excludeFromRecents`, `taskAffinity=""`, `windowSoftInputMode=stateAlwaysVisible`) — most reliable for focus; background start allowed with SYSTEM_ALERT_WINDOW. Side effects: YouTube gets `onPause`; check it does not enter PiP or leave the video; `finish()` must return cleanly.
- In the EditText: `OnKeyListener` — Enter without Shift saves, Shift+Enter newline, Esc cancels. Watch Huawei's IME eating Enter in hardware-keyboard mode.
- Device facts (MateBridge NOTES): a third-party IME (SwiftKey) ate hardware-keyboard letters before `Activity.dispatchKeyEvent` (fix there: `FLAG_ALT_FOCUSABLE_IM` + `dispatchKeyEventPreIme` router); Huawei HiWrite puts a handwriting layer over visible EditTexts and steals pen input; a focused view out of touch mode shows a grey focus highlight (`defaultFocusHighlightEnabled=false`).
- Sources: https://developer.android.com/guide/components/activities/background-starts

## 4. Files

Recommendation: `MANAGE_EXTERNAL_STORAGE` (request via `Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION`), then `FileOutputStream(file, true)` + `mkdirs()`. Fine for a personal sideload. SAF tree URI works but is clunkier (DocumentFile lookups, unreliable "wa" append). Risk: HarmonyOS may place the All-files-access screen elsewhere. MateBridge already uses `MANAGE_EXTERNAL_STORAGE` on this tablet.

## 5. Clipboard

Android 10+ restricts background clipboard **reads**; **writes** (`setPrimaryClip`) are not restricted, and the overlay has focus when Enter is pressed anyway. Source: https://developer.android.com/privacy-and-security/risks/secure-clipboard-handling

## 6. HarmonyOS risks

- Aggressive killing/disabling: users report accessibility services switched off after reboot or RAM clearing. Settings → Battery → App launch → SwiftNote: manage manually, all three on; battery optimisation off; lock in Recents; make the service crash-proof. Sources: https://dontkillmyapp.com/huawei
- Re-check the accessibility toggle after every `adb install -r`.

## Uncertainties for the spike (ranked, with pass checks)

1. **Hardware keys reach `onKeyEvent`.** Log every KeyEvent with YouTube fullscreen. Pass: Ctrl+Shift+N arrives as down/up with meta state; returning `true` stops YouTube/system from reacting; Huawei shortcuts do not swallow it first. **Also pass: in MateBridge, typing and shortcuts reach the Mac unchanged with the service on.**
2. **Focused overlay takes typing over fullscreen.** Pass: typing lands immediately without a tap, caret visible, YouTube stays fullscreen, Esc and Enter/Shift+Enter reachable. Fail → B, then C.
3. **YouTube session metadata.** Pass: TITLE and ARTIST non-null; position after `pause()` matches the on-screen time within ~1 s; `play()` resumes. Record whether MEDIA_ID holds an 11-char video ID.
4. **Translucent-Activity side effects** (only if C is needed). Pass: no PiP, no jump to home feed; `finish()` + `play()` returns to the same fullscreen video.
5. **Survival.** Reboot, overnight idle, swipe from Recents, `adb install -r`. Pass: both toggles still on and the shortcut works each time.
6. **Storage path.** Pass: two appends → one file; UTF-8 Turkish name intact; visible over MateBridge Wi-Fi in Finder.
7. **Share intent.** Pass: YouTube Share lists SwiftNote; EXTRA_TEXT has a parseable video URL.
8. **SDK level.** Pass: `getprop ro.build.version.sdk` returns 31 or 32. If ≥33, re-check Android 13 restricted settings.
