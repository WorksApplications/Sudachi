#!/bin/bash
# Run benchmark with CommonCrawl (raw HTML)

set -eux
DIR=$(dirname "$(readlink -f "$0")")

CRAWL_DATE=${1:-"2024-33"}
LINE=${2:-"1"}  # use n-th file in path file
NUM_RECORDS=${3:-"1000"}  # take first n records

# Download CommonCrawl
DATA_DIR="$DIR/data/cc${CRAWL_DATE}"
mkdir -p "$DATA_DIR"

CCURL="https://data.commoncrawl.org"
BASEURL="${CCURL}/crawl-data/CC-MAIN-${CRAWL_DATE}"

PATHFILE="${DATA_DIR}/warc.paths"
if [ ! -e "${PATHFILE}" ]; then
    curl -L "${BASEURL}/warc.paths.gz" | gzip -dc > $PATHFILE
fi

CORPUS_WARC="$DATA_DIR/${LINE}.warc"
FILEURL="${CCURL}/$(head ${PATHFILE} -n ${LINE} | tail -n 1)"
if [ ! -e "${CORPUS_WARC}" ]; then
    curl -L "$FILEURL" | gzip -dc > $CORPUS_WARC
fi

# extract HTML
CORPUS_WARC="$DATA_DIR/${LINE}.warc"
CORPUS_FILE="$DATA_DIR/${LINE}.txt"
python process_warc.py -i ${CORPUS_WARC} -o ${CORPUS_FILE} -n ${NUM_RECORDS}

# setup & run
$DIR/benchmark_setup.sh
$DIR/benchmark_run.sh $CORPUS_FILE "commoncrawl_${CRAWL_DATE}_${LINE}_${NUM_RECORDS}"
