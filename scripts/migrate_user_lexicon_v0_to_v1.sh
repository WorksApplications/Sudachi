#!/bin/bash -
# convert V0 user lexicon csv file into V1 format
set -eux

# Constants
DIR=$(dirname "$(readlink -f "$0")")
SUDACHI_VERSION=$(${DIR}/../gradlew properties --console=plain -q | grep "^version:" | awk '{printf $2}')

# args
LEXICON_FILE=${1}
SYSTEM_DICT=${2}

# Build Sudachi
${DIR}/../gradlew build -q

BUILD_DIR="$DIR/../build/distributions"
JAR_DIR="$BUILD_DIR/sudachi"
if [ -e "$JAR_DIR" ]; then
    rm -r "$JAR_DIR"
fi
unzip -q -d "$JAR_DIR" "$BUILD_DIR/sudachi-executable-$SUDACHI_VERSION.zip"

# Build and Print
DATA_DIR=$JAR_DIR/dictdata
mkdir -p "$DATA_DIR"

USER_DICT="${DATA_DIR}/migrating.dic"

java -Dfile.encoding=UTF-8 \
    -cp "$JAR_DIR/sudachi-${SUDACHI_VERSION}.jar" \
    com.worksap.nlp.sudachi.dictionary.UserDictionaryBuilder \
    -o "$USER_DICT" -s "$SYSTEM_DICT" "$LEXICON_FILE"

java -Dfile.encoding=UTF-8 \
    -cp "$JAR_DIR/sudachi-${SUDACHI_VERSION}.jar" \
    com.worksap.nlp.sudachi.dictionary.DictionaryPrinter \
    --posMode PARTS \
    --wordRefMode TRIPLE_PARTS \
    -s "$SYSTEM_DICT" "$USER_DICT"
