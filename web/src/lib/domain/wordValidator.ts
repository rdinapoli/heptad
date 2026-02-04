import type { Puzzle } from '../models/puzzle';
import { getAllLetters } from '../models/puzzle';

export type ValidationResult =
  | { type: 'valid' }
  | { type: 'tooShort' }
  | { type: 'missingCenter' }
  | { type: 'invalidLetters' }
  | { type: 'notInWordList' }
  | { type: 'alreadyFound' };

export function validationMessage(result: ValidationResult): string {
  switch (result.type) {
    case 'valid': return 'Valid word';
    case 'tooShort': return 'Too short — words must be 4+ letters';
    case 'missingCenter': return 'Missing center letter';
    case 'invalidLetters': return 'Uses letters not in the puzzle';
    case 'notInWordList': return 'Not in word list';
    case 'alreadyFound': return 'Already found';
  }
}

export function validate(
  puzzle: Puzzle,
  word: string,
  foundWords: Set<string>
): ValidationResult {
  const w = word.toLowerCase().trim();
  const allLetters = getAllLetters(puzzle);

  if (w.length < 4) return { type: 'tooShort' };
  if (!w.includes(puzzle.centerLetter)) return { type: 'missingCenter' };
  if ([...w].some(ch => !allLetters.has(ch))) return { type: 'invalidLetters' };
  if (!puzzle.validWords.has(w)) return { type: 'notInWordList' };
  if (foundWords.has(w)) return { type: 'alreadyFound' };
  return { type: 'valid' };
}

export function calculateScore(puzzle: Puzzle, word: string): number {
  const w = word.toLowerCase().trim();
  if (!puzzle.validWords.has(w)) return 0;
  const baseScore = w.length === 4 ? 1 : w.length;
  const pangramBonus = puzzle.pangrams.has(w) ? 7 : 0;
  return baseScore + pangramBonus;
}

export function isPangram(puzzle: Puzzle, word: string): boolean {
  return puzzle.pangrams.has(word.toLowerCase().trim());
}

export function getSuccessMessage(puzzle: Puzzle, word: string): string {
  const score = calculateScore(puzzle, word);
  return isPangram(puzzle, word) ? `PANGRAM! +${score} points` : `+${score} points`;
}
