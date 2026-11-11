#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
sh build.sh test
printf 'obsolete resource\n' > build/classes/obsolete-resource.txt
printf 'obsolete class\n' > build/classes/RemovedFeature.class
printf 'obsolete test\n' > build/test-classes/RemovedTest.class
sh build.sh test
jar --list --file build/restaurant-rush.jar > build/jar-entries.txt
if grep -Eq '^(obsolete-resource.txt|RemovedFeature.class)$' build/jar-entries.txt \
   || [ -e build/test-classes/RemovedTest.class ]; then
    echo 'A rebuild retained deleted classes or resources.' >&2
    exit 1
fi
echo 'PASS: rebuild excludes obsolete classes, tests and resources'
