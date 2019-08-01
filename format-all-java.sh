#!/bin/bash

set -e

FORMATTER="google-java-format"
RELEASE="${FORMATTER}-1.7"
JAR="${RELEASE}-all-deps.jar"
URL="https://github.com/google/$FORMATTER/releases/download/$RELEASE/$JAR"

BASEPATH="$(dirname "$(realpath "$0")")"
JARPATH="$BASEPATH/$JAR"
cd "$BASEPATH"

if [[ ! -f "$JAR" ]]
then
  echo "Downloading $JAR"
  wget -q "$URL"
fi

PROJECTS=("exampleLang" "mal.lib" "mal.cli" "mal.test" "mal.img")

for project in "${PROJECTS[@]}"
do
  PROJECT_PATH="$BASEPATH/$project"

  if [[ -d "$PROJECT_PATH" ]]
  then
    cd "$PROJECT_PATH"

    if [[ -d "src" ]]
    then
      cd "src"
      echo "Formatting $(find . -name "*.java" | wc -l) files in $project..."

      PIDS=()

      for f in `find . -name "*.java"`
      do
        java -jar "$JARPATH" -i "$f" &
        PIDS+=($!)
      done

      for pid in "${PIDS[@]}"
      do
        wait "$pid"
      done
    fi
  fi
done
echo "Done"
