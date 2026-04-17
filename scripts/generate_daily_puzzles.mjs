#!/usr/bin/env node
/**
 * Extend app/src/main/res/raw/daily_puzzles.json with deterministically
 * generated puzzles covering a wide future window. Preserves any
 * hand-curated puzzles already in the file (by puzzle_number) and fills
 * in the rest using the same fallback logic the apps use at runtime.
 *
 * Usage: node scripts/generate_daily_puzzles.mjs [--count=N]
 *   --count=N  total number of puzzles in the output (default 800,
 *              roughly 2+ years past start_date 2026-01-15)
 */

import fs from 'node:fs';
import path from 'node:path';
import url from 'node:url';

const __dirname = path.dirname(url.fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const ASSET_PATH = path.join(ROOT, 'app/src/main/res/raw/daily_puzzles.json');
const DICT_PATH = path.join(ROOT, 'app/src/main/res/raw/scowl_70.txt');

// --- Args ---
const args = Object.fromEntries(
  process.argv.slice(2).map(a => {
    const m = a.match(/^--(\w+)=(.*)$/);
    return m ? [m[1], m[2]] : [a.replace(/^--/, ''), true];
  })
);
const TARGET_COUNT = Number.parseInt(args.count ?? '800', 10);

// --- PRNG (matches web/src/lib/utils/seededRandom.ts) ---
function createSeededRandom(seed) {
  let s = seed | 0;
  return function () {
    s = (s + 0x6d2b79f5) | 0;
    let t = Math.imul(s ^ (s >>> 15), 1 | s);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function shuffle(arr, randomFn) {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(randomFn() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

// --- Generator (matches web/src/lib/domain/dailyPuzzleGenerator.ts
//     and app/.../PuzzleGenerationHelper.kt) ---
const RARE = new Set(['q', 'x', 'z', 'j', 'k']);
const DICT_LEVEL = 70;
const INCLUDE_S = true;
const SEED_SALT_LO = 0x41445f56;

const EMERGENCY_LETTER_SETS = [
  ['a', 'e', 'i', 'n', 'r', 's', 't'],
  ['a', 'e', 'o', 'l', 'n', 'r', 's'],
  ['a', 'e', 'i', 'l', 'n', 's', 't'],
  ['a', 'e', 'o', 'r', 's', 't', 'd'],
  ['a', 'e', 'i', 'g', 'n', 'r', 't'],
];

function scoreLetters(letters) {
  let score = 0;
  const rareCount = letters.filter(l => RARE.has(l)).length;
  if (rareCount > 1) score -= (rareCount - 1) * 50;
  if (letters.includes('q') && !letters.includes('u')) score -= 100;
  return score;
}

function selectLetters(random) {
  const alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');
  const pool = INCLUDE_S ? alphabet : alphabet.filter(l => l !== 's');
  for (let i = 0; i < 10; i++) {
    const candidate = shuffle(pool, random).slice(0, 7);
    if (scoreLetters(candidate) >= 0) return candidate;
  }
  return shuffle(pool, random).slice(0, 7);
}

function findValidWords(dictionary, letters, center) {
  const result = new Set();
  for (const word of dictionary) {
    if (word.length < 4) continue;
    if (!word.includes(center)) continue;
    let ok = true;
    for (const ch of word) if (!letters.has(ch)) { ok = false; break; }
    if (ok) result.add(word);
  }
  return result;
}

function findBestCenter(letters, dictionary) {
  const letterSet = new Set(letters);
  let bestCenter = null, bestWords = null, bestCount = 0;
  for (const c of letters) {
    const words = findValidWords(dictionary, letterSet, c);
    if (words.size > bestCount) { bestCount = words.size; bestCenter = c; bestWords = words; }
  }
  return bestCenter ? [bestCenter, bestWords] : null;
}

function findPangrams(validWords, allLetters) {
  const result = new Set();
  const L = [...allLetters];
  for (const w of validWords) {
    if (L.every(l => w.includes(l))) result.add(w);
  }
  return result;
}

function calculateMaxScore(validWords, pangrams) {
  let total = 0;
  for (const w of validWords) {
    const base = w.length === 4 ? 1 : w.length;
    const bonus = pangrams.has(w) ? 7 : 0;
    total += base + bonus;
  }
  return total;
}

function generateForDate(dateIso, puzzleNumber, dictionary) {
  const date = new Date(dateIso + 'T00:00:00');
  const epochDay = Math.floor(date.getTime() / 86400000);
  const baseSeed = ((epochDay * 2654435761) ^ SEED_SALT_LO) | 0;

  // Tier 1: strict (20+ words, 1+ pangram)
  for (let attempt = 0; attempt < 200; attempt++) {
    const random = createSeededRandom(baseSeed + attempt);
    const letters = selectLetters(random);
    const result = findBestCenter(letters, dictionary);
    if (!result) continue;
    const [center, validWords] = result;
    const letterSet = new Set(letters);
    const pangrams = findPangrams(validWords, letterSet);
    if (validWords.size >= 20 && pangrams.size > 0) {
      return buildEntry(puzzleNumber, letters, center, validWords, pangrams);
    }
  }

  // Tier 2: relaxed (10+ words, pangram optional)
  for (let attempt = 0; attempt < 100; attempt++) {
    const random = createSeededRandom(baseSeed + 1000 + attempt);
    const letters = selectLetters(random);
    const result = findBestCenter(letters, dictionary);
    if (!result) continue;
    const [center, validWords] = result;
    const letterSet = new Set(letters);
    const pangrams = findPangrams(validWords, letterSet);
    if (validWords.size >= 10) {
      return buildEntry(puzzleNumber, letters, center, validWords, pangrams);
    }
  }

  // Tier 3: emergency vowel-heavy letter set
  const idx =
    ((epochDay % EMERGENCY_LETTER_SETS.length) + EMERGENCY_LETTER_SETS.length) %
    EMERGENCY_LETTER_SETS.length;
  const letters = EMERGENCY_LETTER_SETS[idx];
  const [center, validWords] = findBestCenter(letters, dictionary);
  const letterSet = new Set(letters);
  const pangrams = findPangrams(validWords, letterSet);
  return buildEntry(puzzleNumber, letters, center, validWords, pangrams);
}

function buildEntry(puzzleNumber, letters, center, validWords, pangrams) {
  const outer = letters.filter(l => l !== center);
  return {
    puzzle_number: puzzleNumber,
    center_letter: center,
    outer_letters: outer,
    valid_words: [...validWords].sort(),
    pangrams: [...pangrams].sort(),
    max_score: calculateMaxScore(validWords, pangrams),
  };
}

// --- Main ---
function main() {
  const asset = JSON.parse(fs.readFileSync(ASSET_PATH, 'utf8'));
  const startDate = asset.start_date;
  const existing = new Map(asset.puzzles.map(p => [p.puzzle_number, p]));
  const preservedCount = existing.size;

  console.log(`Loaded asset with ${preservedCount} existing puzzles (start_date=${startDate}).`);
  console.log(`Target count: ${TARGET_COUNT}. Generating ${TARGET_COUNT - preservedCount} new puzzles...`);

  const dictionary = new Set(
    fs.readFileSync(DICT_PATH, 'utf8')
      .split('\n')
      .map(l => l.trim().toLowerCase())
      .filter(w => w.length >= 4 && /^[a-z]+$/.test(w))
  );
  console.log(`Loaded ${dictionary.size} dictionary words.`);

  const startMs = new Date(startDate + 'T00:00:00').getTime();
  const out = [];
  for (let n = 1; n <= TARGET_COUNT; n++) {
    if (existing.has(n)) {
      out.push(existing.get(n));
      continue;
    }
    const dayOffset = n - 1;
    const dateMs = startMs + dayOffset * 86400000;
    const d = new Date(dateMs);
    const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    const entry = generateForDate(iso, n, dictionary);
    out.push(entry);
    if (n % 100 === 0) console.log(`  generated ${n}/${TARGET_COUNT}`);
  }

  const result = {
    version: asset.version,
    start_date: asset.start_date,
    puzzles: out,
  };
  fs.writeFileSync(ASSET_PATH, JSON.stringify(result, null, 2) + '\n');
  console.log(`Wrote ${out.length} puzzles to ${path.relative(ROOT, ASSET_PATH)}.`);
  console.log(`  preserved: ${preservedCount}`);
  console.log(`  generated: ${out.length - preservedCount}`);
}

main();
