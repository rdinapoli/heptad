import { createPuzzle, type Puzzle } from '../models/puzzle';

const MIN_WORDS = 10;
const MAX_WORDS = 200;
const MIN_SCORE = 20;
const MAX_SCORE = 500;
const MAX_ATTEMPTS = 200;
const RARE_LETTERS = new Set(['q', 'x', 'z', 'j', 'k']);
const MAX_RARE_LETTERS = 1;

function shuffle<T>(arr: T[], randomFn: () => number = Math.random): T[] {
  const a = [...arr];
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(randomFn() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

export function scoreLetterCombination(letters: string[]): number {
  let score = 0;
  const rareCount = letters.filter(l => RARE_LETTERS.has(l)).length;
  if (rareCount > MAX_RARE_LETTERS) {
    score -= (rareCount - MAX_RARE_LETTERS) * 50;
  }
  if (letters.includes('q') && !letters.includes('u')) {
    score -= 100;
  }
  return score;
}

export function selectLetters(includeS: boolean, randomFn: () => number = Math.random): string[] {
  const alphabet = 'abcdefghijklmnopqrstuvwxyz'.split('');
  const pool = includeS ? alphabet : alphabet.filter(l => l !== 's');

  for (let i = 0; i < 10; i++) {
    const candidate = shuffle(pool, randomFn).slice(0, 7);
    if (scoreLetterCombination(candidate) >= 0) return candidate;
  }
  return shuffle(pool, randomFn).slice(0, 7);
}

export function findValidWords(
  dictionary: Set<string>,
  letters: Set<string>,
  centerLetter: string
): Set<string> {
  const result = new Set<string>();
  for (const word of dictionary) {
    if (word.length < 4) continue;
    if (!word.includes(centerLetter)) continue;
    if ([...word].every(ch => letters.has(ch))) {
      result.add(word);
    }
  }
  return result;
}

export function findBestCenter(
  letters: string[],
  dictionary: Set<string>
): [string, Set<string>] | null {
  const letterSet = new Set(letters);
  let bestCenter: string | null = null;
  let bestWords: Set<string> | null = null;
  let bestCount = 0;

  for (const center of letters) {
    const words = findValidWords(dictionary, letterSet, center);
    if (words.size > bestCount) {
      bestCount = words.size;
      bestCenter = center;
      bestWords = words;
    }
  }

  if (bestCenter && bestWords && bestCount > 0) {
    return [bestCenter, bestWords];
  }
  return null;
}

export function findPangrams(validWords: Set<string>, allLetters: Set<string>): Set<string> {
  const result = new Set<string>();
  for (const word of validWords) {
    if ([...allLetters].every(l => word.includes(l))) {
      result.add(word);
    }
  }
  return result;
}

export function calculateMaxScore(validWords: Set<string>, pangrams: Set<string>): number {
  let total = 0;
  for (const word of validWords) {
    const base = word.length === 4 ? 1 : word.length;
    const bonus = pangrams.has(word) ? 7 : 0;
    total += base + bonus;
  }
  return total;
}

export function generatePuzzle(
  dictionary: Set<string>,
  level: number = 70,
  includeS: boolean = true,
  commonDictionary?: Set<string>
): Puzzle | null {
  for (let attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
    const letters = selectLetters(includeS);
    const result = findBestCenter(letters, dictionary);
    if (!result) continue;

    const [bestCenter, bestWords] = result;
    const letterSet = new Set(letters);
    const pangrams = findPangrams(bestWords, letterSet);
    const maxScore = calculateMaxScore(bestWords, pangrams);

    if (bestWords.size < MIN_WORDS || bestWords.size > MAX_WORDS) continue;
    if (maxScore < MIN_SCORE || maxScore > MAX_SCORE) continue;
    if (pangrams.size === 0) continue;
    if (scoreLetterCombination(letters) < -50) continue;

    // Check pangram quality
    if (commonDictionary) {
      let hasCommon = false;
      for (const p of pangrams) {
        if (commonDictionary.has(p)) { hasCommon = true; break; }
      }
      if (!hasCommon && pangrams.size > 0) {
        // Allow if only obscure pangrams, but with lower priority
      }
    }

    const outerLetters = letters.filter(l => l !== bestCenter);
    return createPuzzle({
      centerLetter: bestCenter,
      outerLetters,
      validWords: bestWords,
      pangrams,
      maxScore,
      dictionaryLevel: level,
      includesS: includeS
    });
  }

  return null;
}
