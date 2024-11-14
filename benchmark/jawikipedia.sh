#!/bin/bash
# Run benchmark with Japanese Wikipedia (first 100M articles)

set -eux
DIR=$(dirname "$(readlink -f "$0")")

DUMP_DATE=${1:-"20240801"}
SIZE=${2:-"100M"}

# Download Wikipedia dump (ja)
DATA_DIR=$DIR/data/jawiki_${DUMP_DATE}
mkdir -p "$DATA_DIR"

## full dump is too large (>15GB), take first split.
BASEURL="https://dumps.wikimedia.org/jawiki/${DUMP_DATE}"
FILEURL="${BASEURL}/jawiki-${DUMP_DATE}-pages-articles1.xml-p1p114794.bz2"
CORPUS_XML="$DATA_DIR/jawiki_${DUMP_DATE}_1.xml"

if [ ! -e "$CORPUS_XML" ]; then
    curl -L $FILEURL | bzip2 -dc > $CORPUS_XML
fi

# extract
CORPUS_FILE="$DATA_DIR/wiki_00"

## assume wikiextracutor is installed (https://github.com/attardi/wikiextractor)
if [ ! -e "$CORPUS_FILE" ]; then
    python -m wikiextractor.WikiExtractor $CORPUS_XML -o $DATA_DIR -b ${SIZE}
    mv $DATA_DIR/AA/* $DATA_DIR
    rm -r "$DATA_DIR/AA"
fi

# setup & run
$DIR/benchmark_setup.sh
$DIR/benchmark_run.sh $CORPUS_FILE "jawiki_${DUMP_DATE}_${SIZE}"

