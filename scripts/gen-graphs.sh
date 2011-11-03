#!/bin/sh

# Script to generate ImageJ dependency graphs using maven-graph-plugin.
# Called automatically by Jenkins during the ImageJ-daily job.
# Requires graphviz to be installed.

# Usage: bash scripts/gen-graphs.sh

set -e

ROOT=`cd "$(dirname $0)/.." ; pwd`
COMPONENTS=(. core ui)

GRAPH_CMD="
  neato
  -Goverlap=scale
  -Gsplines=true
  -Gepsilon=0.0001
  -Tjpg
  -o dependency-graph.jpg
  dependency-graph.dot
"

for c in ${COMPONENTS[*]}
do
  cd $ROOT/$c
  mvn graph:reactor
  cd target && $GRAPH_CMD
done
