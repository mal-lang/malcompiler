#!/bin/bash

set -e

MAL_DIR="$(dirname "$(dirname "$(realpath "$0")")")"
LIB_DIR="$MAL_DIR/malcompiler-lib"
CLI_DIR="$MAL_DIR/malcompiler-cli"
MOJO_DIR="$MAL_DIR/mal-maven-plugin"
TEST_DIR="$MAL_DIR/malcompiler-test"
JLINK_DIR="$MAL_DIR/malcompiler-jlink"
UPDATE_DIR="$MAL_DIR/update_mal"

cd "$MAL_DIR"
mvn clean verify --quiet --batch-mode

cd "$LIB_DIR"
mvn clean install --quiet --batch-mode

cd "$CLI_DIR"
mvn clean install --quiet --batch-mode

cd "$MOJO_DIR"
mvn clean install --quiet --batch-mode

cd "$TEST_DIR"
mvn clean install --quiet --batch-mode

cd "$JLINK_DIR"
mvn clean install --quiet --batch-mode

cd "$UPDATE_DIR"
./run-tests.sh

cd "$MAL_DIR"
mvn clean verify sonar:sonar --quiet --batch-mode \
  --define sonar.projectKey=mal-lang_malcompiler
