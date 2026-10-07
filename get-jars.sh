#!/usr/bin/env bash
# Downloads the 5 Lucene jars this workshop needs into ./lib  (macOS / Linux / Codespaces)
set -e
V=10.5.0
mkdir -p lib
for a in core analysis-common queryparser queries sandbox; do
  echo "Downloading lucene-$a-$V.jar"
  curl -fsSL -o "lib/lucene-$a-$V.jar" \
    "https://repo1.maven.org/maven2/org/apache/lucene/lucene-$a/$V/lucene-$a-$V.jar"
done
echo "Done. Now run:  java -cp \"lib/*\" src/Workshop.java \"search engine\""
