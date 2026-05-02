#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="${ROOT_DIR}/.tools"
DOWNLOADS_DIR="${TOOLS_DIR}/downloads"

GRADLE_VERSION="${GRADLE_VERSION:-9.4.1}"
ANDROID_CLI_TOOLS_VERSION="${ANDROID_CLI_TOOLS_VERSION:-11076708}"
ANDROID_API_LEVEL="${ANDROID_API_LEVEL:-35}"
ANDROID_BUILD_TOOLS_VERSION="${ANDROID_BUILD_TOOLS_VERSION:-35.0.0}"

GRADLE_HOME="${TOOLS_DIR}/gradle-${GRADLE_VERSION}"
ANDROID_HOME="${ANDROID_HOME:-${TOOLS_DIR}/android-sdk}"
SDKMANAGER="${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager"
GRADLE="${GRADLE_HOME}/bin/gradle"

mkdir -p "${DOWNLOADS_DIR}" "${ANDROID_HOME}/cmdline-tools"

download_if_missing() {
  local url="$1"
  local output="$2"

  if [[ ! -f "${output}" ]]; then
    echo "Downloading ${url}"
    curl --fail --location --output "${output}" "${url}"
  fi
}

if [[ ! -x "${GRADLE}" ]]; then
  gradle_zip="${DOWNLOADS_DIR}/gradle-${GRADLE_VERSION}-bin.zip"
  download_if_missing \
    "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
    "${gradle_zip}"
  rm -rf "${GRADLE_HOME}"
  unzip -q "${gradle_zip}" -d "${TOOLS_DIR}"
fi

if [[ ! -x "${SDKMANAGER}" ]]; then
  cli_zip="${DOWNLOADS_DIR}/commandlinetools-linux-${ANDROID_CLI_TOOLS_VERSION}_latest.zip"
  tmp_cli_dir="${TOOLS_DIR}/cmdline-tools-tmp"
  download_if_missing \
    "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CLI_TOOLS_VERSION}_latest.zip" \
    "${cli_zip}"
  rm -rf "${tmp_cli_dir}" "${ANDROID_HOME}/cmdline-tools/latest"
  mkdir -p "${tmp_cli_dir}"
  unzip -q "${cli_zip}" -d "${tmp_cli_dir}"
  mv "${tmp_cli_dir}/cmdline-tools" "${ANDROID_HOME}/cmdline-tools/latest"
  rm -rf "${tmp_cli_dir}"
fi

export ANDROID_HOME
export ANDROID_SDK_ROOT="${ANDROID_HOME}"
export PATH="${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/cmdline-tools/latest/bin:${GRADLE_HOME}/bin:${PATH}"

yes | "${SDKMANAGER}" --licenses >/dev/null || true
"${SDKMANAGER}" \
  "platform-tools" \
  "platforms;android-${ANDROID_API_LEVEL}" \
  "build-tools;${ANDROID_BUILD_TOOLS_VERSION}"

cd "${ROOT_DIR}"
"${GRADLE}" --no-daemon :app:assembleDebug

echo
echo "Debug APK built at: ${ROOT_DIR}/app/build/outputs/apk/debug/app-debug.apk"
