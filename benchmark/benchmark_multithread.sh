#!/bin/bash
# Analyze given file n-times in multithread.
# assume `benchmark_setup.sh` is called beforehand.

set -eux
DIR=$(dirname "$(readlink -f "$0")")
cd "${DIR}/.."

SUDACHI_VERSION=$(./gradlew properties --console=plain -q | grep "^version:" | awk '{printf $2}')

CORPUS_FILE=$1
NUM_THREAD=${2:-3}
DICT_TYPE=${3:-"small"}

# Build code
BUILD_DIR="$DIR/../build/distributions"
JAR_FILE="$BUILD_DIR/sudachi/sudachi-${SUDACHI_VERSION}.jar"
SRC_ROOT="${DIR}/src"
SRC_DIR="${SRC_ROOT}/com/worksap/nlp/sudachi/benchmark"
SRC_NAME="TokenizeMultiThread"

if [ ! -e "${SRC_DIR}/${SRC_NAME}.class" ]; then
    javac -cp ${JAR_FILE} ${SRC_DIR}/${SRC_NAME}.java
fi

# Run
cd ${DIR}
DATA_DIR=$DIR/data
LOGFILE="$DATA_DIR/benchmark.log"

echo "$(date), $SUDACHI_VERSION, multithread ${NUM_THREAD}, ${DICT_TYPE}, begin" >> $LOGFILE
echo $(ls -l $CORPUS_FILE) >> $LOGFILE

java -Dfile.encoding=UTF-8 -cp ${SRC_ROOT}:${JAR_FILE} \
    com.worksap.nlp.sudachi.benchmark.${SRC_NAME} \
    --systemDict ${DIR}/data/system_${DICT_TYPE}.dic \
    -p "$NUM_THREAD" "$CORPUS_FILE" > /dev/null

echo "$(date), $SUDACHI_VERSION, multithread ${NUM_THREAD}, ${DICT_TYPE}, end" >> $LOGFILE
