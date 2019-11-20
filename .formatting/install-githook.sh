#!/bin/bash

set -e

OLD_PWD="$PWD"
cd "$(dirname "$0")/.."
CWD="$PWD"
cd "$OLD_PWD"

FMT_DIR="$CWD/.formatting"
HOOKS_DIR="$CWD/.git/hooks"

if [ ! -d "$FMT_DIR" ]; then
  >&2 echo "$FMT_DIR: no such directory"
  exit 1
fi

if [ ! -d "$HOOKS_DIR" ]; then
  >&2 echo "$HOOKS_DIR: no such directory"
  exit 1
fi

FMT_PRE_COMMIT_FILE="$FMT_DIR/pre-commit"

if [ ! -f "$FMT_PRE_COMMIT_FILE" ]; then
  >&2 echo "$FMT_PRE_COMMIT_FILE: no such file"
  exit 1
fi

HOOKS_PRE_COMMIT_FILE="$HOOKS_DIR/pre-commit"

cp "$FMT_PRE_COMMIT_FILE" "$HOOKS_PRE_COMMIT_FILE"
chmod a+x "$HOOKS_PRE_COMMIT_FILE"
