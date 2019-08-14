#!/bin/bash

OLD_MAL="old-spec.mal"
NEW_MAL="new-spec.mal"
TMP_MAL="tmp-spec.mal"
RUN_UPDATE="python3 update_mal.py"

function run_test {
  CMD="$1"
  eval "$CMD"
  if [[ "$(diff "$NEW_MAL" "$TMP_MAL")" != "" ]]; then
    >&2 echo "Error: \"$CMD\" failed"
  fi
  rm -f "$TMP_MAL"
}

CWD="$(dirname "$(realpath "$0")")"
cd "$CWD"

# Test input from file, output to file
run_test "$RUN_UPDATE -i $OLD_MAL -o $TMP_MAL"

# Test input from file, output to stdout
run_test "$RUN_UPDATE -i $OLD_MAL -o - > $TMP_MAL"

# Test input from stdin, output to file
run_test "$RUN_UPDATE -i - -o $TMP_MAL < $OLD_MAL"

# Test input from stdin, output to stdout
run_test "$RUN_UPDATE -i - -o - < $OLD_MAL > $TMP_MAL"
