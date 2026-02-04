#!/usr/bin/env python3
"""
Split definitions.gz from Android res/raw/ into 26 letter-chunked JSON files
for the web app's on-demand loading.

Input:  app/src/main/res/raw/definitions.gz
Output: web/public/data/definitions/a.json ... z.json
"""

import gzip
import json
import os
import sys

def main():
    root_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    src_file = os.path.join(root_dir, 'app', 'src', 'main', 'res', 'raw', 'definitions.gz')
    dst_dir = os.path.join(root_dir, 'web', 'public', 'data', 'definitions')

    if not os.path.exists(src_file):
        print(f"Error: {src_file} not found")
        sys.exit(1)

    os.makedirs(dst_dir, exist_ok=True)

    print(f"Reading {src_file}...")
    with gzip.open(src_file, 'rt', encoding='utf-8') as f:
        data = json.load(f)

    print(f"Loaded {len(data)} definitions")

    # Group by first letter
    buckets: dict[str, dict] = {}
    for letter in 'abcdefghijklmnopqrstuvwxyz':
        buckets[letter] = {}

    for word, definition in data.items():
        word_lower = word.lower()
        if word_lower and word_lower[0].isalpha():
            letter = word_lower[0]
            # Format: list of {pos, def} objects -> group by pos into meanings
            if isinstance(definition, list):
                by_pos: dict[str, list] = {}
                for entry in definition:
                    pos = entry.get('pos', '')
                    d = entry.get('def', '')
                    by_pos.setdefault(pos, []).append({'definition': d})
                buckets[letter][word_lower] = {
                    'word': word_lower,
                    'meanings': [
                        {'partOfSpeech': pos, 'definitions': defs}
                        for pos, defs in by_pos.items()
                    ]
                }
            elif isinstance(definition, str):
                buckets[letter][word_lower] = {
                    'word': word_lower,
                    'meanings': [{
                        'partOfSpeech': '',
                        'definitions': [{'definition': definition}]
                    }]
                }

    # Write each letter chunk
    total = 0
    for letter, words in buckets.items():
        out_file = os.path.join(dst_dir, f'{letter}.json')
        with open(out_file, 'w', encoding='utf-8') as f:
            json.dump(words, f, separators=(',', ':'))
        size = os.path.getsize(out_file)
        total += len(words)
        print(f"  {letter}.json: {len(words)} words ({size:,} bytes)")

    print(f"\nTotal: {total} definitions split into 26 files")

if __name__ == '__main__':
    main()
