#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
sdk_dir="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
android_jar="${MCW_ANDROID_JAR:-${sdk_dir:+$sdk_dir/platforms/android-35/android.jar}}"
build_tools="${MCW_BUILD_TOOLS:-${sdk_dir:+$sdk_dir/build-tools/35.0.1}}"
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
for tool in javac java keytool zip openssl shasum; do
  command -v "$tool" >/dev/null || { echo "Missing command: $tool" >&2; exit 1; }
done
for tool in aapt2 d8 zipalign apksigner; do
  [[ -x "$build_tools/$tool" ]] || { echo "Set MCW_BUILD_TOOLS to your Android Build-Tools directory (missing $tool)." >&2; exit 1; }
done
[[ -f "$android_jar" ]] || { echo "Set ANDROID_HOME or MCW_ANDROID_JAR to locate Android 35 android.jar." >&2; exit 1; }
mkdir -p "$project_dir/build" "$project_dir/dist"
build_dir="$(mktemp -d "$project_dir/build/run.XXXXXX")"
mkdir -p "$build_dir/classes" "$build_dir/dex" "$build_dir/generated"
"$build_tools/aapt2" compile --dir "$project_dir/res" -o "$build_dir/resources.zip"
"$build_tools/aapt2" link -o "$build_dir/unsigned.apk" -I "$android_jar" \
  --manifest "$project_dir/AndroidManifest.xml" --java "$build_dir/generated" \
  "$build_dir/resources.zip"
javac -encoding UTF-8 --release 8 -classpath "$android_jar" -d "$build_dir/classes" \
  "$project_dir/src/com/wbdl/elder/ui/HomeActivity.java" \
  "$project_dir/src/org/magiccaster/research/launcher/LauncherActivity.java"
classes=()
while IFS= read -r class_file; do classes+=("$class_file"); done < <(find "$build_dir/classes" -name '*.class' -type f)
"$build_tools/d8" --lib "$android_jar" --min-api 28 --output "$build_dir/dex" "${classes[@]}"
(cd "$build_dir/dex" && zip -q "$build_dir/unsigned.apk" classes.dex)
"$build_tools/zipalign" -f 4 "$build_dir/unsigned.apk" "$build_dir/aligned.apk"

key_dir="${MCW_SIGNING_DIR:-${XDG_DATA_HOME:-$HOME/.local/share}/magic-caster-devices-launcher/signing}"
mkdir -p "$key_dir"
chmod 700 "$key_dir"
keystore="$key_dir/launcher.p12"
password_file="$key_dir/password"
if [[ ! -f "$keystore" ]]; then
  if [[ -e "$password_file" ]]; then
    echo "Password exists without a key; inspect it instead of overwriting." >&2
    exit 1
  fi
  (umask 077; openssl rand -hex -out "$password_file" 32)
  keytool -genkeypair -noprompt -storetype PKCS12 -keystore "$keystore" \
    -storepass:file "$password_file" -keypass:file "$password_file" \
    -alias caster-launcher -keyalg RSA -keysize 3072 -validity 10000 \
    -dname 'CN=Magic Caster Community Launcher'
  chmod 600 "$keystore"
fi
output="$project_dir/dist/MagicCaster-Devices-Launcher-0.1.0.apk"
"$build_tools/apksigner" sign --ks "$keystore" --ks-key-alias caster-launcher \
  --ks-pass "file:$password_file" --v1-signing-enabled false --v2-signing-enabled true \
  --v3-signing-enabled true --v4-signing-enabled false --out "$output" "$build_dir/aligned.apk"
"$build_tools/apksigner" verify --verbose --print-certs "$output"
"$build_tools/zipalign" -c 4 "$output"
chmod 644 "$output"
shasum -a 256 "$output"
echo "Built $output"
