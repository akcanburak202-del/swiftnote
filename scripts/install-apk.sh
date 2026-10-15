#!/usr/bin/env bash
# Installs the SwiftNote APK on the tablet without the user tapping the confirmation dialogs, then starts SwiftNote.
#
# HarmonyOS shows up to two confirmations during `adb install`. This script taps ONLY these two buttons, matched
# by package + resource-id (never by text; see docs/NOTES.md 2026-10-01 01:35, a text match once installed
# unwanted store apps):
#   1. com.android.packageinstaller  android:id/button1                                   ("DEVAM ET")
#   2. com.huawei.appmarket          com.huawei.appmarket:id/hidden_card_install_button_continue  (bottom "YÜKLE")
# Nothing else on an AppGallery screen is ever tapped.
#
# Usage: scripts/install-apk.sh [--debug | path/to/app.apk]
#   e.g. a probe: scripts/install-apk.sh probes/storage/app/build/outputs/apk/debug/app-debug.apk
#   The package name and launchable activity are read from the APK itself (aapt2 from the SDK build-tools).
#   default: the non-debuggable "daily" APK (ADR 0003); --debug installs the debug APK (run-as, diagnostics).
#   Both are signed with the debug key, so either updates the other in place with `adb install -r -d`.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/app/build/outputs/apk"
case "${1:-}" in
  --debug) APK="$OUT/debug/app-debug.apk" ;;
  "") APK="$OUT/daily/app-daily.apk" ;;
  *) APK="$1" ;;
esac
[ -f "$APK" ] || { echo "APK not found: $APK" >&2; exit 1; }
# Package and launcher activity come from the APK, never guessed: a wrong package would verify and start the wrong app.
SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
AAPT2="${AAPT2:-$(ls -d "$SDK"/build-tools/*/aapt2 2>/dev/null | sort -V | tail -1)}"
[ -n "$AAPT2" ] && [ -x "$AAPT2" ] || { echo "aapt2 not found under $SDK/build-tools (set AAPT2=/path/to/aapt2)" >&2; exit 1; }
badging="$("$AAPT2" dump badging "$APK" 2>/dev/null)" || { echo "aapt2 could not read $APK" >&2; exit 1; }
PKG="$(printf '%s\n' "$badging" | sed -n "s/^package: name='\([^']*\)'.*/\1/p" | head -1)"
[ -n "$PKG" ] || { echo "could not read the package name from $APK" >&2; exit 1; }
LAUNCH_ACTIVITY="$(printf '%s\n' "$badging" | sed -n "s/^launchable-activity: name='\([^']*\)'.*/\1/p" | head -1)"
echo "package $PKG, launcher activity ${LAUNCH_ACTIVITY:-none}"
# Never fall back to an adb on PATH: a different adb version restarts the running server and drops MateBridge's
# `adb reverse` tunnels (AGENTS.md rule 4). $ADB, else the SDK's platform-tools.
ADB="${ADB:-${ANDROID_HOME:-$HOME/Library/Android/sdk}/platform-tools/adb}"
[ -x "$ADB" ] || { echo "adb not found or not executable: $ADB (set ADB=/path/to/adb)" >&2; exit 1; }
TIMEOUT_S="${INSTALL_TIMEOUT_S:-180}"

"$ADB" get-state >/dev/null 2>&1 || { echo "no device (adb get-state failed)" >&2; exit 1; }
# Only the HUAWEI tablet: another Android device on the cable (e.g. the user's phone) must never get the APK.
brand="$("$ADB" shell getprop ro.product.brand | tr -d '\r')"
[ "$brand" = "HUAWEI" ] || { echo "connected device is not the HUAWEI tablet (brand=$brand); not installing" >&2; exit 1; }

UI_DUMP=/sdcard/sn-ui.xml  # on-screen text (could include typed text): never left on the device
log=""
install_pid=""
cleanup() {
  local rc=$?
  trap - EXIT INT TERM
  if [ -n "$install_pid" ]; then  # only the adb install client this script started
    kill "$install_pid" 2>/dev/null || true
    wait "$install_pid" 2>/dev/null || true
  fi
  [ -z "$log" ] || rm -f "$log"
  "$ADB" shell rm -f "$UI_DUMP" >/dev/null 2>&1 || true
  exit $rc
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

before="$("$ADB" shell dumpsys package "$PKG" | sed -n 's/.*lastUpdateTime=//p' | head -1)"
log="$(mktemp -t sn-install)"
# -d: versionCode is the build minute (ADR 0003), so a freshly built APK always installs over an older one. -d still lets a
# debuggable installed app take an older APK; a non-debuggable (daily) one refuses (rebuild instead). Never uninstall:
# that deletes the app data (draft notes).
"$ADB" install -r -d "$APK" >"$log" 2>&1 &
install_pid=$!

# Prints "x y" for the centre of the first allowed button on screen, or nothing.
find_button() {
  # Dump, read and delete in one device-side command, so the file exists only for an instant.
  # (Streaming with `uiautomator dump /dev/tty` would avoid the file but is untested on API 31; kept file-and-delete.)
  "$ADB" exec-out "rm -f $UI_DUMP; uiautomator dump $UI_DUMP >/dev/null 2>&1 && cat $UI_DUMP; rm -f $UI_DUMP" 2>/dev/null | python3 -c '
import re, sys, xml.etree.ElementTree as ET
ALLOWED = {
    ("com.android.packageinstaller", "android:id/button1"),
    ("com.huawei.appmarket", "com.huawei.appmarket:id/hidden_card_install_button_continue"),
}
try:
    root = ET.fromstring(sys.stdin.read())
except ET.ParseError:
    sys.exit(0)
for n in root.iter("node"):
    if (n.get("package"), n.get("resource-id")) in ALLOWED and n.get("enabled") == "true":
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", n.get("bounds", ""))
        if m:
            x1, y1, x2, y2 = map(int, m.groups())
            print((x1 + x2) // 2, (y1 + y2) // 2, n.get("package"), n.get("resource-id"))
            break
'
}

start=$SECONDS
while kill -0 "$install_pid" 2>/dev/null; do
  if (( SECONDS - start > TIMEOUT_S )); then
    kill "$install_pid" 2>/dev/null || true
    echo "install timed out after ${TIMEOUT_S}s" >&2
    break
  fi
  # Installed already: the AppGallery page then shows "BİTTİ"; tapping it is optional, `am start` below covers it.
  now_ts="$("$ADB" shell dumpsys package "$PKG" | sed -n 's/.*lastUpdateTime=//p' | head -1)"
  if [ "$now_ts" != "$before" ]; then break; fi
  read -r x y pkg rid < <(find_button) || true
  if [ -n "${x:-}" ]; then
    echo "tap $pkg $rid at $x,$y"
    "$ADB" shell input tap "$x" "$y"
    sleep 2
  else
    sleep 1
  fi
  x=""
done
kill "$install_pid" 2>/dev/null || true  # done (or timed out): do not wait on the AppGallery page
wait "$install_pid" 2>/dev/null || true
install_pid=""
cat "$log"
if grep -q INSTALL_FAILED_VERSION_DOWNGRADE "$log"; then
  echo "VERSION_DOWNGRADE: the installed app is non-debuggable (daily) and has a higher versionCode than this APK." >&2
  echo "versionCode is build-time minutes (ADR 0003): rebuild the APK now (a fresh build is always newer). Do NOT uninstall: that deletes the app data (draft notes)." >&2
fi
rm -f "$log"; log=""

after="$("$ADB" shell dumpsys package "$PKG" | sed -n 's/.*lastUpdateTime=//p' | head -1)"
if [ "$after" = "$before" ]; then
  echo "NOT installed (lastUpdateTime still $after)" >&2
  exit 1
fi
echo "installed: lastUpdateTime $before -> $after"
if [ -n "$LAUNCH_ACTIVITY" ]; then
  "$ADB" shell am start -n "$PKG/$LAUNCH_ACTIVITY" >/dev/null
  echo "started $PKG/$LAUNCH_ACTIVITY"
else
  echo "no launchable activity in the APK; not starting $PKG"
fi
