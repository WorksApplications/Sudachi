#!/bin/bash
# Tokenize given file, with each of small/core/full dict and A/B/C mode.
# assume `benchmark_setup.sh` is called beforehand.

set -eux
DIR=$(dirname "$(readlink -f "$0")")
cd "${DIR}/.."

CORPUS_FILE=$1
TASK=${2:-"benchmark"}

SUDACHI_VERSION=$(./gradlew properties --console=plain -q | grep "^version:" | awk '{printf $2}')

# Run benchmark
DATA_DIR=$DIR/data
JAR_DIR="$DIR/../build/distributions/sudachi"
LOGFILE="$DATA_DIR/benchmark.log"

DICT_TYPES=("small" "core" "full")
SPLIT_MODES=("A" "B" "C")

echo "" >> $LOGFILE
echo "$(date), $SUDACHI_VERSION, $TASK, begin" >> $LOGFILE
echo $(ls -l $CORPUS_FILE) >> $LOGFILE
for TYPE in ${DICT_TYPES[@]}; do
    DICT_FILE="$DATA_DIR/system_${TYPE}.dic"
    for MODE in ${SPLIT_MODES[@]}; do
        echo "$(date), $TYPE, $MODE, begin" >> $LOGFILE
        java -Dfile.encoding=UTF-8 -jar "$JAR_DIR/sudachi-${SUDACHI_VERSION}.jar" \
            --systemDict "$DICT_FILE" -m ${MODE} -a \
            "$CORPUS_FILE" > /dev/null
        echo "$(date), $TYPE, $MODE, end" >> $LOGFILE
    done
done
echo "$(date), $SUDACHI_VERSION, $TASK, end" >> $LOGFILE
