import type { Puzzle } from '../models/puzzle';

export interface TwoLetterHint {
  prefix: string;
  totalCount: number;
  foundCount: number;
}

export interface GridCell {
  letter: string;
  length: number;
  totalCount: number;
  foundCount: number;
}

export interface LetterRevealHint {
  partialWord: string;
  fullWord: string;
  fullLength: number;
  isPangram: boolean;
}

export function generateTwoLetterHints(puzzle: Puzzle, foundWords: Set<string>): TwoLetterHint[] {
  const foundLower = new Set([...foundWords].map(w => w.toLowerCase()));

  const allByPrefix = new Map<string, number>();
  for (const word of puzzle.validWords) {
    const prefix = word.slice(0, 2).toUpperCase();
    allByPrefix.set(prefix, (allByPrefix.get(prefix) ?? 0) + 1);
  }

  const foundByPrefix = new Map<string, number>();
  for (const word of foundLower) {
    if (puzzle.validWords.has(word)) {
      const prefix = word.slice(0, 2).toUpperCase();
      foundByPrefix.set(prefix, (foundByPrefix.get(prefix) ?? 0) + 1);
    }
  }

  const hints: TwoLetterHint[] = [];
  for (const [prefix, total] of allByPrefix) {
    hints.push({
      prefix,
      totalCount: total,
      foundCount: foundByPrefix.get(prefix) ?? 0
    });
  }
  return hints.sort((a, b) => a.prefix.localeCompare(b.prefix));
}

export function generateGridHints(puzzle: Puzzle, foundWords: Set<string>): GridCell[] {
  const foundLower = new Set([...foundWords].map(w => w.toLowerCase()));
  const startingLetters = [...new Set([...puzzle.validWords].map(w => w[0].toUpperCase()))].sort();
  const lengths = [...new Set([...puzzle.validWords].map(w => w.length))].sort((a, b) => a - b);

  const cells: GridCell[] = [];
  for (const letter of startingLetters) {
    for (const length of lengths) {
      const matching = [...puzzle.validWords].filter(
        w => w[0].toUpperCase() === letter && w.length === length
      );
      if (matching.length > 0) {
        const foundCount = matching.filter(w => foundLower.has(w)).length;
        cells.push({ letter, length, totalCount: matching.length, foundCount });
      }
    }
  }
  return cells;
}

export function getWordLengths(puzzle: Puzzle): number[] {
  return [...new Set([...puzzle.validWords].map(w => w.length))].sort((a, b) => a - b);
}

export function getStartingLetters(puzzle: Puzzle): string[] {
  return [...new Set([...puzzle.validWords].map(w => w[0].toUpperCase()))].sort();
}

export function generateLetterRevealHints(
  puzzle: Puzzle,
  foundWords: Set<string>,
  revealCount: number = 2
): LetterRevealHint[] {
  const foundLower = new Set([...foundWords].map(w => w.toLowerCase()));
  const unfound = [...puzzle.validWords].filter(w => !foundLower.has(w));

  return unfound
    .map(word => ({
      partialWord: word.slice(0, revealCount).toUpperCase() + '·'.repeat(word.length - revealCount),
      fullWord: word,
      fullLength: word.length,
      isPangram: puzzle.pangrams.has(word)
    }))
    .sort((a, b) => a.partialWord.localeCompare(b.partialWord) || a.fullLength - b.fullLength);
}

export function getRandomUnfoundWord(
  puzzle: Puzzle,
  foundWords: Set<string>,
  excludeWords: Set<string> = new Set()
): string | null {
  const foundLower = new Set([...foundWords].map(w => w.toLowerCase()));
  const excludeLower = new Set([...excludeWords].map(w => w.toLowerCase()));
  const candidates = [...puzzle.validWords].filter(w => !foundLower.has(w) && !excludeLower.has(w));
  if (candidates.length === 0) return null;
  return candidates[Math.floor(Math.random() * candidates.length)];
}

export function getProgressPercentage(puzzle: Puzzle, currentScore: number): number {
  if (puzzle.maxScore === 0) return 0;
  return currentScore / puzzle.maxScore;
}
