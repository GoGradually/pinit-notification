#!/usr/bin/env bash
set -euo pipefail

PROFILES="${SPRING_PROFILES_ACTIVE:-backfill-public-id}"

./gradlew bootRun --args="--spring.profiles.active=${PROFILES} --spring.main.web-application-type=none"
