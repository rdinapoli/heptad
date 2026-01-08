# Dictionary Setup Guide

## Overview

This guide explains how to obtain, process, and integrate the SCOWL dictionary and definitions into the Heptad app.

## Part 1: SCOWL Word List

### Download SCOWL

**Source**: https://github.com/en-wl/wordlist

**Download Options:**

1. **Pre-built releases** (Recommended):
   ```bash
   wget http://downloads.sourceforge.net/wordlist/scowl-2020.12.07.tar.gz
   tar -xzf scowl-2020.12.07.tar.gz
   ```

2. **Clone repository**:
   ```bash
   git clone https://github.com/en-wl/wordlist.git
   cd wordlist/scowl
   ```

### Understanding SCOWL Levels

SCOWL provides words at different "size" levels (0-95):

- **Size 10-35**: Very common words (~10k-30k words)
- **Size 40-55**: Common words (~50k-80k words)
- **Size 60**: Includes some rare/archaic (~127k words) ⭐ Good starting point
- **Size 70**: More scientific/technical (~157k words) ⭐ **Recommended**
- **Size 80**: Very comprehensive (~216k words) ⭐ For experts
- **Size 90-95**: Extremely obscure/archaic (not recommended)

### Extract Words for Specific Levels

SCOWL organizes files by:
- **Variant**: American English (our target)
- **Category**: words, proper-names, upper, etc.
- **Size**: The numeric level

**File naming pattern**: `american-words.{size}`

#### Extract Level 60
```bash
cd scowl/final
cat american-words.10 \
    american-words.20 \
    american-words.35 \
    american-words.40 \
    american-words.50 \
    american-words.60 \
    > scowl-60.txt
```

#### Extract Level 70 (Recommended)
```bash
cat american-words.10 \
    american-words.20 \
    american-words.35 \
    american-words.40 \
    american-words.50 \
    american-words.60 \
    american-words.70 \
    > scowl-70.txt
```

#### Extract Level 80
```bash
cat american-words.10 \
    american-words.20 \
    american-words.35 \
    american-words.40 \
    american-words.50 \
    american-words.60 \
    american-words.70 \
    american-words.80 \
    > scowl-80.txt
```

### Filter Words for Heptad

Create a processing script to filter words:

```python
#!/usr/bin/env python3
# process_scowl.py

def process_wordlist(input_file, output_file, min_length=4, max_length=15):
    """
    Filter SCOWL word list for Heptad requirements
    """
    with open(input_file, 'r', encoding='utf-8') as f:
        words = f.read().splitlines()
    
    filtered_words = []
    
    for word in words:
        # Convert to lowercase
        word = word.strip().lower()
        
        # Skip if empty
        if not word:
            continue
            
        # Skip if length out of range
        if len(word) < min_length or len(word) > max_length:
            continue
            
        # Skip if contains non-alphabetic characters
        if not word.isalpha():
            continue
            
        # Skip if contains capital letters (proper nouns)
        if any(c.isupper() for c in word):
            continue
            
        # Skip contractions with apostrophes
        if "'" in word:
            continue
            
        # Skip hyphenated words
        if "-" in word:
            continue
            
        filtered_words.append(word)
    
    # Remove duplicates and sort
    filtered_words = sorted(set(filtered_words))
    
    # Write output
    with open(output_file, 'w', encoding='utf-8') as f:
        for word in filtered_words:
            f.write(word + '\n')
    
    print(f"Processed {len(filtered_words)} words")
    print(f"Output written to {output_file}")

if __name__ == "__main__":
    # Process all three levels
    for level in [60, 70, 80]:
        print(f"\nProcessing level {level}...")
        process_wordlist(
            f"scowl-{level}.txt",
            f"scowl-{level}-filtered.txt"
        )
```

**Run the script:**
```bash
python3 process_scowl.py
```

This will create:
- `scowl-60-filtered.txt` (~115k words)
- `scowl-70-filtered.txt` (~145k words)
- `scowl-80-filtered.txt` (~200k words)

### Alternative: Use Existing Processed Lists

If you prefer pre-processed lists, you can use:

**ENABLE word list** (172k words, commonly used for word games):
```bash
wget https://raw.githubusercontent.com/dolph/dictionary/master/enable1.txt
```

**Dwyl English Words** (479k words, very comprehensive):
```bash
wget https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt
```

Then apply the same filtering script above.

## Part 2: Word Definitions

### Option 1: WordNet (Recommended)

**Princeton WordNet** is a lexical database with ~120k word-definition pairs.

#### Download WordNet
```bash
wget http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz
tar -xzf wn3.1.dict.tar.gz
```

#### Convert WordNet to JSON

WordNet uses a complex format. Here's a parser:

```python
#!/usr/bin/env python3
# parse_wordnet.py

import re
import json

def parse_wordnet_data(data_file):
    """
    Parse WordNet data file to extract definitions
    """
    definitions = {}
    
    with open(data_file, 'r', encoding='utf-8') as f:
        for line in f:
            # Skip comments
            if line.startswith('  '):
                continue
            
            # Parse line
            # Format: synset_offset lex_filenum ss_type w_cnt word definitions...
            parts = line.split('|')
            if len(parts) < 2:
                continue
            
            # Extract word and definition
            synset_info = parts[0].strip().split()
            definition = parts[1].strip()
            
            # Get words from synset (they're hex-encoded)
            # This is simplified; actual parsing is more complex
            words = re.findall(r'\b[a-z_]+\b', parts[0].lower())
            
            for word in words:
                word = word.replace('_', ' ')
                if word and len(word) >= 4:
                    if word not in definitions:
                        definitions[word] = {
                            'word': word,
                            'definition': definition,
                            'part_of_speech': get_pos(synset_info[2]) if len(synset_info) > 2 else 'unknown'
                        }
    
    return definitions

def get_pos(code):
    """Convert WordNet POS code to readable form"""
    pos_map = {
        'n': 'noun',
        'v': 'verb',
        'a': 'adjective',
        's': 'adjective satellite',
        'r': 'adverb'
    }
    return pos_map.get(code, 'unknown')

if __name__ == "__main__":
    # Process different POS files
    all_definitions = {}
    
    for pos_file in ['data.noun', 'data.verb', 'data.adj', 'data.adv']:
        print(f"Processing {pos_file}...")
        defs = parse_wordnet_data(f"dict/{pos_file}")
        all_definitions.update(defs)
    
    # Write to JSON
    with open('definitions.json', 'w', encoding='utf-8') as f:
        json.dump(all_definitions, f, indent=2)
    
    print(f"Extracted {len(all_definitions)} definitions")
```

**Note**: WordNet parsing is complex. Consider using the NLTK library:

```python
import nltk
from nltk.corpus import wordnet as wn
import json

nltk.download('wordnet')

def extract_definitions():
    definitions = {}
    
    for synset in wn.all_synsets():
        for lemma in synset.lemmas():
            word = lemma.name().replace('_', ' ').lower()
            
            if len(word) >= 4 and word not in definitions:
                definitions[word] = {
                    'word': word,
                    'definition': synset.definition(),
                    'part_of_speech': synset.pos()
                }
    
    return definitions

# Extract and save
defs = extract_definitions()
with open('definitions.json', 'w') as f:
    json.dump(defs, f, indent=2)
```

### Option 2: Wiktionary

**Wiktionary** has comprehensive definitions but requires parsing XML dumps.

#### Download Wiktionary dump
```bash
wget https://dumps.wikimedia.org/enwiktionary/latest/enwiktionary-latest-pages-articles.xml.bz2
bunzip2 enwiktionary-latest-pages-articles.xml.bz2
```

#### Parse with wiktionaryparser library
```bash
pip install wiktionaryparser
```

```python
from wiktionaryparser import WiktionaryParser
import json

parser = WiktionaryParser()

def fetch_definition(word):
    try:
        result = parser.fetch(word)
        if result and len(result) > 0:
            definition = result[0]['definitions']
            if definition and len(definition) > 0:
                return {
                    'word': word,
                    'definition': definition[0]['text'][0] if definition[0]['text'] else '',
                    'part_of_speech': definition[0]['partOfSpeech']
                }
    except:
        pass
    return None

# For your word list
definitions = {}
with open('scowl-70-filtered.txt') as f:
    words = f.read().splitlines()

for i, word in enumerate(words):
    if i % 100 == 0:
        print(f"Processed {i}/{len(words)} words...")
    
    defn = fetch_definition(word)
    if defn:
        definitions[word] = defn

with open('definitions.json', 'w') as f:
    json.dump(definitions, f, indent=2)
```

**Warning**: This will take hours for 150k words. Consider using cached dumps or API rate limiting.

### Option 3: Free Dictionary API

**For smaller word lists or online lookups:**

```python
import requests
import json
import time

def fetch_definition_api(word):
    url = f"https://api.dictionaryapi.dev/api/v2/entries/en/{word}"
    try:
        response = requests.get(url, timeout=5)
        if response.status_code == 200:
            data = response.json()
            if data and len(data) > 0:
                meanings = data[0].get('meanings', [])
                if meanings:
                    return {
                        'word': word,
                        'definition': meanings[0]['definitions'][0]['definition'],
                        'part_of_speech': meanings[0]['partOfSpeech']
                    }
    except:
        pass
    return None

# Rate-limited fetching
definitions = {}
with open('scowl-70-filtered.txt') as f:
    words = f.read().splitlines()[:1000]  # Limit for testing

for word in words:
    defn = fetch_definition_api(word)
    if defn:
        definitions[word] = defn
    time.sleep(0.1)  # Rate limiting
```

### Option 4: Pre-built Definition Databases

**Recommended for quick start:**

```bash
# Simple English definitions (public domain)
wget https://raw.githubusercontent.com/matthewreagan/WebstersEnglishDictionary/master/dictionary.json
```

## Part 3: Integration into Android App

### Directory Structure

```
app/src/main/res/raw/
├── scowl_60.txt
├── scowl_70.txt
├── scowl_80.txt
└── definitions.json
```

### Loading in App

```kotlin
// DictionaryRepository.kt

class DictionaryRepository(private val context: Context) {
    
    private val dictionaryCache = mutableMapOf<Int, Set<String>>()
    private var definitionsCache: Map<String, Definition>? = null
    
    suspend fun loadDictionary(level: Int): Set<String> = withContext(Dispatchers.IO) {
        if (dictionaryCache.containsKey(level)) {
            return@withContext dictionaryCache[level]!!
        }
        
        val resourceId = when (level) {
            60 -> R.raw.scowl_60
            70 -> R.raw.scowl_70
            80 -> R.raw.scowl_80
            else -> R.raw.scowl_70
        }
        
        val words = context.resources.openRawResource(resourceId).bufferedReader().use { reader ->
            reader.readLines().filter { it.isNotBlank() }.toSet()
        }
        
        dictionaryCache[level] = words
        words
    }
    
    suspend fun loadDefinitions(): Map<String, Definition> = withContext(Dispatchers.IO) {
        if (definitionsCache != null) {
            return@withContext definitionsCache!!
        }
        
        val json = context.resources.openRawResource(R.raw.definitions).bufferedReader().use {
            it.readText()
        }
        
        val type = object : TypeToken<Map<String, Definition>>() {}.type
        val definitions: Map<String, Definition> = Gson().fromJson(json, type)
        
        definitionsCache = definitions
        definitions
    }
    
    suspend fun getDefinition(word: String): Definition? {
        val definitions = loadDefinitions()
        return definitions[word.lowercase()]
    }
}
```

## Part 4: File Size Optimization

### Compress Word Lists

Use gzip compression (Android handles this automatically):

```bash
# Compress files before adding to res/raw
gzip scowl-60-filtered.txt
gzip scowl-70-filtered.txt
gzip scowl-80-filtered.txt
gzip definitions.json
```

Then load with:
```kotlin
val inputStream = context.resources.openRawResource(R.raw.scowl_70)
val gzipStream = GZIPInputStream(inputStream)
val words = gzipStream.bufferedReader().use { it.readLines() }
```

### Trim Definitions

For mobile app, keep definitions concise:

```python
def trim_definition(text, max_length=150):
    """Keep definitions under max_length characters"""
    if len(text) <= max_length:
        return text
    
    # Find last complete sentence within limit
    sentences = text.split('. ')
    result = sentences[0]
    
    if len(result) > max_length:
        return result[:max_length-3] + "..."
    
    return result + "."
```

## Part 5: Verification

### Verify Word Count
```bash
wc -l scowl-70-filtered.txt
# Should be ~145,000 lines
```

### Verify Format
```bash
head -20 scowl-70-filtered.txt
# All lowercase, alphabetic only, 4+ letters
```

### Verify Definitions Coverage
```python
import json

with open('scowl-70-filtered.txt') as f:
    words = set(f.read().splitlines())

with open('definitions.json') as f:
    definitions = json.load(f)

coverage = len(set(definitions.keys()) & words) / len(words) * 100
print(f"Definition coverage: {coverage:.1f}%")
# Aim for >60% coverage
```

## Summary Checklist

- [ ] Downloaded SCOWL word lists
- [ ] Extracted levels 60, 70, 80
- [ ] Filtered words (4-15 letters, alpha only)
- [ ] Obtained definition source (WordNet recommended)
- [ ] Parsed definitions to JSON
- [ ] Verified word count and format
- [ ] Placed files in `app/src/main/res/raw/`
- [ ] Implemented loading in DictionaryRepository
- [ ] Tested dictionary loading in app

## Expected File Sizes

- `scowl_60.txt`: ~1.2 MB
- `scowl_70.txt`: ~1.5 MB
- `scowl_80.txt`: ~2.1 MB
- `definitions.json`: ~15-25 MB (compressed: ~3-5 MB)

Total app size impact: ~8-12 MB
