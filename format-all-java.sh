#!/bin/bash

set -e

FORMATTER="google-java-format"
RELEASE="${FORMATTER}-1.7"
JAR="${RELEASE}-all-deps.jar"
URL="https://github.com/google/$FORMATTER/releases/download/$RELEASE/$JAR"

BASEPATH="$(dirname "$(realpath "$0")")"
cd "$BASEPATH"

if [[ ! -f "$JAR" ]]
then
  echo "Downloading $JAR"
  wget -q "$URL"
fi

PIDS=()

echo "Formatting $(find . -name "*.java" | wc -l) files..."

for f in `find . -name "*.java"`
do
  java -jar "$JAR" -i "$f" &
  PIDS+=($!)
done

for pid in "${PIDS[@]}"
do
  wait "$pid"
done

echo "Done"
