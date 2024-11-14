#!/bin/bash
# Build Sudachi and build small/core/full dictionary with it.

set -eux
DIR=$(dirname "$(readlink -f "$0")")
cd "${DIR}/.."

SUDACHI_VERSION=$(./gradlew properties --console=plain -q | grep "^version:" | awk '{printf $2}')

DICT_VERSION=${1:-"20240716"}

# Build Sudachi
./gradlew build
BUILD_DIR="$DIR/../build/distributions"
JAR_DIR="$BUILD_DIR/sudachi"
if [ -e "$JAR_DIR" ]; then
    rm -r "$JAR_DIR"
fi
unzip -d "$JAR_DIR" "$BUILD_DIR/sudachi-executable-$SUDACHI_VERSION.zip"

# Get dictionary data
DATA_DIR=$DIR/data
DICT_DIR=$DIR/data/dictdata
mkdir -p "$DICT_DIR"

RAW_DICT_BASEURL="http://sudachi.s3-website-ap-northeast-1.amazonaws.com/sudachidict-raw"

DICT_FILES=("small_lex" "core_lex" "notcore_lex")
for TYPE in ${DICT_FILES[@]}; do
    if [ ! -e "$DICT_DIR/${TYPE}.csv" ]; then
        ZIPFILE=${TYPE}.zip
        if [ ! -e "$DICT_DIR/$ZIPFILE" ]; then
            wget "$RAW_DICT_BASEURL/$DICT_VERSION/$ZIPFILE" -P $DICT_DIR
        fi
        unzip -d $DICT_DIR $DICT_DIR/$ZIPFILE
    fi
done

MATRIX_FILE="matrix.def"
if [ ! -e "$DICT_DIR/$MATRIX_FILE" ]; then
    ZIPFILE=${MATRIX_FILE}.zip
    if [ ! -e "$ZIPFILE" ]; then
        wget "$RAW_DICT_BASEURL/$ZIPFILE" -P $DICT_DIR
    fi
    unzip -d $DICT_DIR $DICT_DIR/$ZIPFILE
fi

# Build dictionary
DICT_TYPES=("small" "core" "full")

for i in $(seq 0 2); do
    TYPE=${DICT_TYPES[$i]}
    DICT_FILE="$DATA_DIR/system_${TYPE}.dic"
    if [ ! -e "$DICT_FILE" ]; then
        FILES=$(for v in ${DICT_FILES[@]:0:$(expr $i+1)}; do echo "$DICT_DIR/${v}.csv"; done)
        java -Dfile.encoding=UTF-8 -cp "$JAR_DIR/sudachi-${SUDACHI_VERSION}.jar" \
            com.worksap.nlp.sudachi.dictionary.DictionaryBuilder \
            -o "$DICT_FILE" \
            -m "$DICT_DIR/$MATRIX_FILE" \
            $FILES
    fi
done
