#!/usr/bin/env bash

set -eu

export GRADLE_USER_HOME="$(pwd)/.gradle"

echo "Failing deliberately to test the pipeline"
false

version=$(cat version/version)

(
cd source
./gradlew -Pversion=${version} clean build dependencyCheckAnalyze --rerun-tasks --no-daemon
)

cp -a source/* dist/
