#!/bin/bash
# Sync shared assets from Android res/raw/ to web/public/data/
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"
SRC_DIR="$ROOT_DIR/app/src/main/res/raw"
DST_DIR="$ROOT_DIR/web/public/data"

mkdir -p "$DST_DIR"

echo "Syncing assets from $SRC_DIR to $DST_DIR..."

cp "$SRC_DIR/scowl_60.txt" "$DST_DIR/"
cp "$SRC_DIR/scowl_70.txt" "$DST_DIR/"
cp "$SRC_DIR/scowl_80.txt" "$DST_DIR/"
cp "$SRC_DIR/daily_puzzles.json" "$DST_DIR/"

echo "Assets synced successfully."
