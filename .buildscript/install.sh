#!/bin/bash

set -e

MAL_DIR="$(dirname "$(dirname "$(realpath "$0")")")"

cd "$MAL_DIR"
mvn clean install --quiet --batch-mode \
  --define maven.test.skip=true
