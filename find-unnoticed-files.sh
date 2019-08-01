#!/bin/bash

set -e

BASEPATH="$(dirname "$(realpath "$0")")"
cd "$BASEPATH"

for f in `git ls-tree -r HEAD --name-only`
do
  if [[ "$(grep "Copyright 2019 Foreseeti AB" "$f")" == "" ]]
  then
    echo "$f"
  fi
done
