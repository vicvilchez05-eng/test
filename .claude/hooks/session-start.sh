#!/bin/bash
# SessionStart hook: prepares the Android build environment in Claude Code on the web.
# Installs the Android SDK (command-line tools, platform-tools, platform 36, build-tools 35)
# and warms the Gradle cache so ./gradlew works right away. Idempotent.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"

# 1. Command-line tools
if [ ! -x "$SDKMANAGER" ]; then
  echo "[session-start] Installing Android command-line tools into $ANDROID_HOME"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -sSL -o "$tmp/cmdtools.zip" "$CMDLINE_TOOLS_URL"
  unzip -q "$tmp/cmdtools.zip" -d "$tmp"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rm -rf "$tmp"
fi

# 2. Licenses + SDK packages (sdkmanager skips packages that are already installed)
yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" "platform-tools" "platforms;android-36" "build-tools;35.0.0" 2>&1 \
  | grep -vE '^\[|Loading|JAVA_TOOL_OPTIONS' || true

# 3. Point Gradle at the SDK
export ANDROID_HOME ANDROID_SDK_ROOT="$ANDROID_HOME"
{
  echo "export ANDROID_HOME=\"$ANDROID_HOME\""
  echo "export ANDROID_SDK_ROOT=\"$ANDROID_HOME\""
  echo "export PATH=\"\$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin\""
} >> "${CLAUDE_ENV_FILE:-/dev/null}"

if [ ! -f "$PROJECT_DIR/local.properties" ]; then
  echo "sdk.dir=$ANDROID_HOME" > "$PROJECT_DIR/local.properties"
fi

# 4. Warm the Gradle wrapper and dependency cache
cd "$PROJECT_DIR"
./gradlew --console=plain -q compileDebugKotlin compileDebugUnitTestKotlin 2>&1 \
  | grep -vE 'JAVA_TOOL_OPTIONS|Problems report' || true

echo "[session-start] Android environment ready (ANDROID_HOME=$ANDROID_HOME)"
