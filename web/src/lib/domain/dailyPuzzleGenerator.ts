import { createSeededRandom } from '../utils/seededRandom';
import { createPuzzle, type Puzzle } from '../models/puzzle';
import {
  selectLetters,
  findBestCenter,
  findPangrams,
  calculateMaxScore
} from './puzzleGenerator';

const DAILY_DICTIONARY_LEVEL = 70;
const DAILY_INCLUDE_S = true;
const SEED_SALT_LO = 0x41445f56; // Lower 32 bits of SEED_SALT

const EMERGENCY_LETTER_SETS = [
  ['a', 'e', 'i', 'n', 'r', 's', 't'],
  ['a', 'e', 'o', 'l', 'n', 'r', 's'],
  ['a', 'e', 'i', 'l', 'n', 's', 't'],
  ['a', 'e', 'o', 'r', 's', 't', 'd'],
  ['a', 'e', 'i', 'g', 'n', 'r', 't']
];

/**
 * Generate a daily fallback puzzle deterministically from the date.
 * Three-tier strategy:
 * 1. Strict: 200 attempts, require 20+ words and a pangram
 * 2. Relaxed: 100 attempts, require 10+ words
 * 3. Emergency: vowel-heavy letter sets guaranteed to produce words
 */
export function generateDailyFallback(
  date: Date,
  dictionary: Set<string>,
  puzzleNumber: number
): Puzzle {
  const epochDay = Math.floor(date.getTime() / 86400000);
  const baseSeed = ((epochDay * 2654435761) ^ SEED_SALT_LO) | 0;

  // Tier 1: Strict quality (20+ words, pangram required)
  for (let attempt = 0; attempt < 200; attempt++) {
    const random = createSeededRandom(baseSeed + attempt);
    const letters = selectLetters(DAILY_INCLUDE_S, random);
    const result = findBestCenter(letters, dictionary);
    if (!result) continue;

    const [center, validWords] = result;
    const letterSet = new Set(letters);
    const pangrams = findPangrams(validWords, letterSet);

    if (validWords.size >= 20 && pangrams.size > 0) {
      const maxScore = calculateMaxScore(validWords, pangrams);
      return createPuzzle({
        id: `daily-${puzzleNumber}`,
        centerLetter: center,
        outerLetters: letters.filter(l => l !== center),
        validWords,
        pangrams,
        maxScore,
        dictionaryLevel: DAILY_DICTIONARY_LEVEL,
        includesS: DAILY_INCLUDE_S
      });
    }
  }

  // Tier 2: Relaxed quality (10+ words, no pangram required)
  for (let attempt = 0; attempt < 100; attempt++) {
    const random = createSeededRandom(baseSeed + 1000 + attempt);
    const letters = selectLetters(DAILY_INCLUDE_S, random);
    const result = findBestCenter(letters, dictionary);
    if (!result) continue;

    const [center, validWords] = result;
    const letterSet = new Set(letters);
    const pangrams = findPangrams(validWords, letterSet);

    if (validWords.size >= 10) {
      const maxScore = calculateMaxScore(validWords, pangrams);
      return createPuzzle({
        id: `daily-${puzzleNumber}`,
        centerLetter: center,
        outerLetters: letters.filter(l => l !== center),
        validWords,
        pangrams,
        maxScore,
        dictionaryLevel: DAILY_DICTIONARY_LEVEL,
        includesS: DAILY_INCLUDE_S
      });
    }
  }

  // Tier 3: Emergency vowel-heavy letter sets
  const idx =
    ((epochDay % EMERGENCY_LETTER_SETS.length) + EMERGENCY_LETTER_SETS.length) %
    EMERGENCY_LETTER_SETS.length;
  const letters = EMERGENCY_LETTER_SETS[idx];
  const [center, validWords] = findBestCenter(letters, dictionary)!;
  const letterSet = new Set(letters);
  const pangrams = findPangrams(validWords, letterSet);
  const maxScore = calculateMaxScore(validWords, pangrams);
  return createPuzzle({
    id: `daily-${puzzleNumber}`,
    centerLetter: center,
    outerLetters: letters.filter(l => l !== center),
    validWords,
    pangrams,
    maxScore,
    dictionaryLevel: DAILY_DICTIONARY_LEVEL,
    includesS: DAILY_INCLUDE_S
  });
}
