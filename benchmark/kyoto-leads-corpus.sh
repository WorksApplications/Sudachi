#!/bin/bash
# Run benchmark with Kyoto Leads Corpus

set -eux
DIR=$(dirname "$(readlink -f "$0")")

# Download Kyoto Leads corpus original texts
DATA_DIR=$DIR/data
mkdir -p "$DATA_DIR"

CORPUS_FILE="$DATA_DIR/leads.txt"
if [ ! -e "$CORPUS_FILE" ]; then
    curl -L https://github.com/ku-nlp/KWDLC/releases/download/release_1_0/leads.org.txt.gz | gzip -dc > $CORPUS_FILE
fi

# Setup & run
$DIR/benchmark_setup.sh
$DIR/benchmark_run.sh $CORPUS_FILE "kyoto-leads"
