#!/bin/bash

SLUG="mal-lang/malcompiler"
JDK="openjdk11"
BRANCH="master"

set -e

if [ "$TRAVIS_REPO_SLUG" != "$SLUG" ]; then
  echo "Skipping deployment: wrong repository. Expected '$SLUG' but was '$TRAVIS_REPO_SLUG'."
elif [ "$TRAVIS_JDK_VERSION" != "$JDK" ]; then
  echo "Skipping deployment: wrong JDK. Expected '$JDK' but was '$TRAVIS_JDK_VERSION'."
elif [ "$TRAVIS_PULL_REQUEST" != "false" ]; then
  echo "Skipping deployment: was pull request."
elif [ "$TRAVIS_BRANCH" != "$BRANCH" ]; then
  echo "Skipping deployment: wrong branch. Expected '$BRANCH' but was '$TRAVIS_BRANCH'."
else
  echo "Deploying..."
  echo use-agent >> ~/.gnupg/gpg.conf
  echo pinentry-mode loopback >> ~/.gnupg/gpg.conf
  echo allow-loopback-pinentry >> ~/.gnupg/gpg-agent.conf
  echo RELOADAGENT | gpg-connect-agent &> /dev/null
  export GPG_TTY=$(tty)
  echo $GPG_SECRET_KEYS | base64 --decode 2> /dev/null | $GPG_EXECUTABLE --import --no-tty --batch --yes &> /dev/null
  echo $GPG_OWNERTRUST | base64 --decode 2> /dev/null | $GPG_EXECUTABLE --import-ownertrust --no-tty --batch --yes &> /dev/null
  mvn clean deploy --quiet --batch-mode --settings .buildscript/settings.xml --activate-profiles ossrh --define maven.test.skip=true
  echo "Deployed!"
fi
