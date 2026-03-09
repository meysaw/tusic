#!/usr/bin/env bash
#
# Tusic — Linux launch script
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/target/tusic-1.0-SNAPSHOT.jar"

# Ensure binaries are executable
chmod +x "$SCRIPT_DIR/bin/linux/"* 2>/dev/null || true

if [ ! -f "$JAR" ]; then
  echo "ERROR: JAR not found at $JAR"
  echo "Run 'mvn clean package' first."
  exit 1
fi

java -jar "$JAR"
