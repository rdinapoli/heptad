#!/usr/bin/env python3
"""
Generate offline dictionary from WordNet for Heptad app.
Parses WordNet data files and creates a compact JSON definitions file.
"""

import json
import re
import gzip
from pathlib import Path
from collections import defaultdict

WORDNET_DIR = Path(__file__).parent / "dict"
SCOWL_DIR = Path(__file__).parent.parent / "app/src/main/res/raw"
OUTPUT_FILE = Path(__file__).parent.parent / "app/src/main/res/raw/definitions.gz"

# Part of speech mapping
POS_MAP = {
    'n': 'noun',
    'v': 'verb',
    'a': 'adjective',
    's': 'adjective',  # satellite adjective
    'r': 'adverb'
}

def get_base_forms(word):
    """
    Generate possible base forms for an inflected word.
    Returns list of (base_form, inflection_type) tuples.
    """
    candidates = []

    # Plurals
    if word.endswith('ies') and len(word) > 4:
        candidates.append((word[:-3] + 'y', 'plural'))
    if word.endswith('es') and len(word) > 3:
        candidates.append((word[:-2], 'plural'))
    if word.endswith('s') and not word.endswith('ss') and len(word) > 2:
        candidates.append((word[:-1], 'plural'))

    # Past tense / past participle
    if word.endswith('ied') and len(word) > 4:
        candidates.append((word[:-3] + 'y', 'past'))
    if word.endswith('ed') and len(word) > 3:
        candidates.append((word[:-2], 'past'))  # walked -> walk
        candidates.append((word[:-1], 'past'))  # baked -> bake
        # Doubled consonant: stopped -> stop
        if len(word) > 4 and word[-3] == word[-4]:
            candidates.append((word[:-3], 'past'))

    # Present participle (-ing)
    if word.endswith('ing') and len(word) > 4:
        candidates.append((word[:-3], 'participle'))  # walking -> walk
        candidates.append((word[:-3] + 'e', 'participle'))  # baking -> bake
        # Doubled consonant: running -> run
        if len(word) > 5 and word[-4] == word[-5]:
            candidates.append((word[:-4], 'participle'))

    # Agent nouns (-er, -or)
    if word.endswith('ers') and len(word) > 4:
        candidates.append((word[:-3], 'agent_plural'))
        candidates.append((word[:-3] + 'e', 'agent_plural'))
    if word.endswith('er') and len(word) > 3:
        candidates.append((word[:-2], 'agent'))
        candidates.append((word[:-2] + 'e', 'agent'))
        # Doubled: runner -> run
        if len(word) > 4 and word[-3] == word[-4]:
            candidates.append((word[:-3], 'agent'))

    # Superlative/comparative (-est, -er for adjectives handled above)
    if word.endswith('est') and len(word) > 4:
        candidates.append((word[:-3], 'superlative'))
        candidates.append((word[:-3] + 'e', 'superlative'))
        if len(word) > 5 and word[-4] == word[-5]:
            candidates.append((word[:-4], 'superlative'))

    # Adverbs (-ly)
    if word.endswith('ly') and len(word) > 4:
        candidates.append((word[:-2], 'adverb'))
        if word.endswith('ily'):
            candidates.append((word[:-3] + 'y', 'adverb'))

    return candidates

def get_inflected_pos(inflection_type):
    """Map inflection type to part of speech."""
    pos_map = {
        'plural': 'noun',
        'past': 'verb',
        'participle': 'verb',
        'agent': 'noun',
        'agent_plural': 'noun',
        'superlative': 'adjective',
        'comparative': 'adjective',
        'adverb': 'adverb'
    }
    return pos_map.get(inflection_type, 'word')

def generate_inflected_definition(base_word, base_defs, inflection_type):
    """Generate a definition for an inflected form."""
    # Get base definition for context
    base_pos = base_defs[0]['pos'] if base_defs else 'word'

    templates = {
        'plural': f"plural of {base_word}",
        'past': f"past tense of {base_word}",
        'participle': f"present participle of {base_word}",
        'agent': f"one who {base_word}s" if base_pos == 'verb' else f"person or thing that {base_word}s",
        'agent_plural': f"plural of {base_word}er",
        'superlative': f"superlative of {base_word}",
        'comparative': f"comparative of {base_word}",
        'adverb': f"in a {base_word} manner"
    }

    return {
        'pos': get_inflected_pos(inflection_type),
        'def': templates.get(inflection_type, f"form of {base_word}")
    }

def load_scowl_words():
    """Load all words from SCOWL dictionaries."""
    words = set()
    for level in [60, 70, 80]:
        scowl_file = SCOWL_DIR / f"scowl_{level}.txt"
        if scowl_file.exists():
            with open(scowl_file, 'r') as f:
                for line in f:
                    word = line.strip().lower()
                    if len(word) >= 4:  # Only 4+ letter words matter for the game
                        words.add(word)
    print(f"Loaded {len(words)} unique words from SCOWL dictionaries")
    return words

def parse_data_file(filepath, pos_char):
    """Parse a WordNet data file and extract definitions."""
    definitions = {}

    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            # Skip license header lines
            if line.startswith('  '):
                continue

            parts = line.strip().split(' | ')
            if len(parts) < 2:
                continue

            # Parse the data line
            header = parts[0].split()
            if len(header) < 5:
                continue

            # Get gloss (definition + examples)
            gloss = parts[1] if len(parts) > 1 else ""

            # Extract just the definition (before any example in quotes)
            definition = gloss.split('"')[0].strip()
            definition = definition.rstrip(';').strip()

            if not definition:
                continue

            # Get word count and extract words
            try:
                synset_offset = header[0]
                lex_filenum = header[1]
                ss_type = header[2]
                w_cnt = int(header[3], 16)  # hex

                # Extract words from synset
                idx = 4
                for i in range(w_cnt):
                    if idx >= len(header):
                        break
                    word = header[idx].lower().replace('_', ' ')
                    # Skip multi-word phrases
                    if ' ' not in word:
                        if word not in definitions:
                            definitions[word] = []
                        # Store (pos, definition) - limit to first definition per POS
                        pos = POS_MAP.get(ss_type, 'noun')
                        existing_pos = [d[0] for d in definitions[word]]
                        if pos not in existing_pos:
                            definitions[word].append((pos, definition))
                    idx += 2  # skip word and lex_id
            except (ValueError, IndexError):
                continue

    return definitions

def merge_definitions(all_defs):
    """Merge definitions from all POS into single dict."""
    merged = defaultdict(list)
    for defs in all_defs:
        for word, meanings in defs.items():
            for pos, definition in meanings:
                # Check if we already have this POS for this word
                existing = [m for m in merged[word] if m['pos'] == pos]
                if not existing:
                    merged[word].append({
                        'pos': pos,
                        'def': definition[:200]  # Limit definition length
                    })
    return dict(merged)

def main():
    print("Loading SCOWL words...")
    scowl_words = load_scowl_words()

    print("Parsing WordNet data files...")
    all_definitions = []

    for data_file, pos_char in [
        ('data.noun', 'n'),
        ('data.verb', 'v'),
        ('data.adj', 'a'),
        ('data.adv', 'r')
    ]:
        filepath = WORDNET_DIR / data_file
        if filepath.exists():
            print(f"  Parsing {data_file}...")
            defs = parse_data_file(filepath, pos_char)
            all_definitions.append(defs)
            print(f"    Found {len(defs)} words")

    print("Merging definitions...")
    merged = merge_definitions(all_definitions)
    print(f"Total unique words with definitions: {len(merged)}")

    # Filter to only SCOWL words
    print("Filtering to SCOWL words...")
    filtered = {}
    for word in scowl_words:
        if word in merged:
            filtered[word] = merged[word]

    print(f"Words with definitions: {len(filtered)} / {len(scowl_words)}")
    coverage = len(filtered) / len(scowl_words) * 100
    print(f"Coverage: {coverage:.1f}%")

    # Generate definitions for inflected forms
    print("Generating definitions for inflected forms...")
    generated_count = 0

    for word in scowl_words:
        if word in filtered:
            continue  # Already has definition

        # Try to find a base form with a definition
        candidates = get_base_forms(word)
        for base, inflection_type in candidates:
            if base in filtered:
                # Found base with definition - generate inflected definition
                filtered[word] = [generate_inflected_definition(base, filtered[base], inflection_type)]
                generated_count += 1
                break

    print(f"Generated {generated_count} inflected definitions")
    total_with_defs = len(filtered)
    new_coverage = total_with_defs / len(scowl_words) * 100
    print(f"Total words with definitions: {total_with_defs} / {len(scowl_words)}")
    print(f"New coverage: {new_coverage:.1f}%")

    # Create output directory if needed
    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    # Write compressed JSON
    print(f"Writing to {OUTPUT_FILE}...")
    with gzip.open(OUTPUT_FILE, 'wt', encoding='utf-8') as f:
        json.dump(filtered, f, separators=(',', ':'))

    # Also write uncompressed for inspection
    uncompressed = OUTPUT_FILE.with_suffix('')
    with open(uncompressed, 'w', encoding='utf-8') as f:
        json.dump(filtered, f, separators=(',', ':'))

    compressed_size = OUTPUT_FILE.stat().st_size / 1024 / 1024
    uncompressed_size = uncompressed.stat().st_size / 1024 / 1024

    print(f"Uncompressed size: {uncompressed_size:.1f} MB")
    print(f"Compressed size: {compressed_size:.1f} MB")
    print("Done!")

if __name__ == '__main__':
    main()
