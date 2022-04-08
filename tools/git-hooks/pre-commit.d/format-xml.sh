#!/bin/sh

set -eu

cd "$(dirname "$0")/../../.."

echo_changed_xml() {
  set +e
  git diff --cached --name-only --diff-filter=ACMR | grep "\.xml$" | grep -v "pom\.xml$"
  set -e
}

format_xml() {
  # shellcheck source=../../scripts/utils.sh
  . tools/scripts/utils.sh

  echo "Formatting $(echo "$changed_xml" | wc -l) xml files"
  mvn -B -q -Dincludes="$(comma_list "$changed_xml")" xml-format:xml-format
  echo "$changed_xml" | tr "\n" "\0" | xargs -0 git add -f
}

main() {
  changed_xml="$(echo_changed_xml)"
  if [ -z "$changed_xml" ]; then
    exit
  fi
  format_xml
}

main
