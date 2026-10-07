#!/usr/bin/env bash
set -euo pipefail
project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -n "${JAVA_HOME:-}" ]]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
mkdir -p "$project_dir/build"
test_dir="$(mktemp -d "$project_dir/build/test.XXXXXX")"
javac --release 8 -d "$test_dir" \
  "$project_dir/src/com/wbdl/elder/ui/HomeActivity.java" \
  "$project_dir/tests/EnumWireTest.java"
java -cp "$test_dir" EnumWireTest
