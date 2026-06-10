#!/usr/bin/env bash
set -euo pipefail

if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-19-openjdk-amd64}"
exec mvn spring-boot:run
