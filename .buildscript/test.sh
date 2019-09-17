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
echo "Installing 'malcompiler'..."
mvn clean install --quiet --batch-mode

cd "$LIB_DIR"
echo "Installing 'malcompiler-lib'..."
mvn clean install --quiet --batch-mode

cd "$CLI_DIR"
echo "Installing 'malcompiler-cli'..."
mvn clean install --quiet --batch-mode

cd "$MOJO_DIR"
echo "Installing 'mal-maven-plugin'..."
mvn clean install --quiet --batch-mode

cd "$TEST_DIR"
echo "Installing 'malcompiler-test'..."
mvn clean install --quiet --batch-mode

cd "$JLINK_DIR"
echo "Installing 'malcompiler-jlink'..."
mvn clean install --quiet --batch-mode

cd "$UPDATE_DIR"
echo "Testing 'update_mal'..."
./run-tests.sh

cd "$MAL_DIR"
echo "Analyzing project with SonarCloud..."
mvn clean verify sonar:sonar --quiet --batch-mode \
  --define sonar.projectKey=mal-lang_malcompiler
