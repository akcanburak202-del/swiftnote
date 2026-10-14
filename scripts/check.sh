#!/usr/bin/env bash
# Single verification entry point for agents, the orchestrator and CI.
# Usage: scripts/check.sh                 # doc claims + gradle assembleDebug assembleDaily testDebugUnitTest
#        scripts/check.sh --only docs     # doc claims only (no JDK/SDK needed)
#        scripts/check.sh --only android  # gradle only
# Prints "check.sh: ALL OK" on success, otherwise FAIL lines and "check.sh: FAILURES" (exit 1).
set -uo pipefail
cd "$(dirname "$0")/.."

only=""
while [ $# -gt 0 ]; do
  case $1 in
    --only) [ $# -ge 2 ] || { echo "check.sh: --only needs docs|android" >&2; exit 2; }; only=$2; shift 2 ;;
    --only=*) only=${1#--only=}; shift ;;
    -h|--help) sed -n '2,6p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "check.sh: unknown argument '$1'" >&2; exit 2 ;;
  esac
done
case $only in ""|docs|android) ;; *) echo "check.sh: unknown component '$only' (docs, android)" >&2; exit 2 ;; esac
want() { [ -z "$only" ] || [ "$only" = "$1" ]; }

fail=0

# Documentation claims: every repo path named in backticks in the top-level docs must exist.
# Only paths under known top-level directories are checked; placeholders and globs are skipped.
if want docs; then
  echo "==> doc claims"
  for doc in AGENTS.md CLAUDE.md README.md; do
    [ -f "$doc" ] || { echo "    FAIL: $doc missing"; fail=1; continue; }
    # Backtick spans that look like a path (allow a trailing "/" for directories).
    grep -o '`[^` ]*`' "$doc" | tr -d '`' | sort -u | while read -r p; do
      case $p in
        docs/*|scripts/*|backlog/*|probes/*|app/*|.github/*|gradle/*) ;;
        *) continue ;;
      esac
      case $p in *'*'*|*'<'*|*NNNN*|*XXX*|*'~'*|*'{'*) continue ;; esac
      [ -e "$p" ] || { echo "    FAIL: $doc names '$p' which does not exist"; echo x >> .check-doc-fail.tmp; }
    done
  done
  if [ -f .check-doc-fail.tmp ]; then rm -f .check-doc-fail.tmp; fail=1; else echo "    OK: doc claims"; fi
fi

if want android; then
  AS_JBR="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
  if [ -z "${JAVA_HOME:-}" ] && [ -d "$AS_JBR" ]; then export JAVA_HOME="$AS_JBR"; fi
  if [ -z "${ANDROID_HOME:-}" ] && [ -d "$HOME/Library/Android/sdk" ]; then export ANDROID_HOME="$HOME/Library/Android/sdk"; fi
  echo "==> gradle"
  if [ -z "${JAVA_HOME:-}" ] && ! command -v java >/dev/null 2>&1; then
    echo "    FAIL: gradle needs a JDK (install Android Studio or set JAVA_HOME)"; fail=1
  elif ./gradlew --quiet assembleDebug assembleDaily testDebugUnitTest; then
    echo "    OK: gradle"
  else
    echo "    FAIL: gradle"; fail=1
  fi
fi

if [ $fail -eq 0 ]; then echo "check.sh: ALL OK"; else echo "check.sh: FAILURES"; fi
exit $fail
