export interface Puzzle {
  id: string;
  centerLetter: string;
  outerLetters: string[];
  validWords: Set<string>;
  pangrams: Set<string>;
  maxScore: number;
  dictionaryLevel: number;
  includesS: boolean;
}

export function createPuzzle(params: {
  id?: string;
  centerLetter: string;
  outerLetters: string[];
  validWords: string[] | Set<string>;
  pangrams: string[] | Set<string>;
  maxScore: number;
  dictionaryLevel?: number;
  includesS?: boolean;
}): Puzzle {
  return {
    id: params.id ?? crypto.randomUUID(),
    centerLetter: params.centerLetter.toLowerCase(),
    outerLetters: params.outerLetters.map(l => l.toLowerCase()),
    validWords: params.validWords instanceof Set ? params.validWords : new Set(params.validWords.map(w => w.toLowerCase())),
    pangrams: params.pangrams instanceof Set ? params.pangrams : new Set(params.pangrams.map(w => w.toLowerCase())),
    maxScore: params.maxScore,
    dictionaryLevel: params.dictionaryLevel ?? 70,
    includesS: params.includesS ?? true
  };
}

export function getAllLetters(puzzle: Puzzle): Set<string> {
  return new Set([...puzzle.outerLetters, puzzle.centerLetter]);
}

export function isPangram(puzzle: Puzzle, word: string): boolean {
  return puzzle.pangrams.has(word.toLowerCase());
}

export function calculateWordScore(puzzle: Puzzle, word: string): number {
  const w = word.toLowerCase();
  if (!puzzle.validWords.has(w)) return 0;
  const baseScore = w.length === 4 ? 1 : w.length;
  const pangramBonus = puzzle.pangrams.has(w) ? 7 : 0;
  return baseScore + pangramBonus;
}
