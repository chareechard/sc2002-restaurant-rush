#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
if [ -L build ]; then echo 'Build directory must not be a symbolic link.' >&2; exit 2; fi
rm -rf -- build/classes build/test-classes
mkdir -p build/classes build/test-classes
find src/main/java -name '*.java' > build/sources.txt
javac --release 17 -encoding UTF-8 -Xlint:all -Werror -d build/classes @build/sources.txt
jar --create --file build/restaurant-rush.jar --main-class rush.cli.ConsoleApp -C build/classes .
task="${1:-build}"
if [ "$#" -gt 0 ]; then shift; fi
case "$task" in
  build) ;;
  run) java -jar build/restaurant-rush.jar "$@" ;;
  test)
    find src/test/java -name '*.java' > build/tests.txt
    javac --release 17 -encoding UTF-8 -Xlint:all -Werror -cp build/classes -d build/test-classes @build/tests.txt
    java -ea -cp build/classes:build/test-classes rush.AllTests ;;
  *) echo 'Usage: sh build.sh [build|run|test] [game arguments]' >&2; exit 2 ;;
esac
