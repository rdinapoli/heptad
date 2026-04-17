import { Rank } from './rank';

export enum HintTier {
  TWO_LETTER_LIST = 'TWO_LETTER_LIST',
  GRID_VIEW = 'GRID_VIEW',
  LETTER_REVEAL = 'LETTER_REVEAL',
  DEFINITION_HINT = 'DEFINITION_HINT'
}

export const HINT_TIER_THRESHOLDS: Record<HintTier, number> = {
  [HintTier.TWO_LETTER_LIST]: 0.0,
  [HintTier.GRID_VIEW]: 0.25,
  [HintTier.LETTER_REVEAL]: 0.40,
  [HintTier.DEFINITION_HINT]: 0.60
};

export const HINT_TIER_NAMES: Record<HintTier, string> = {
  [HintTier.TWO_LETTER_LIST]: 'Two-Letter List',
  [HintTier.GRID_VIEW]: 'Grid View',
  [HintTier.LETTER_REVEAL]: 'Letter Reveal',
  [HintTier.DEFINITION_HINT]: 'Definition Hint'
};

export interface HintState {
  unlockedTiers: Set<HintTier>;
  expandedTiers: Set<HintTier>;
}

export function createHintState(): HintState {
  return {
    unlockedTiers: new Set([HintTier.TWO_LETTER_LIST]),
    expandedTiers: new Set()
  };
}

export function updateHintUnlocks(
  state: HintState,
  wordsFound: number,
  totalWords: number,
  forceAll: boolean = false
): HintState {
  if (forceAll) {
    return { ...state, unlockedTiers: new Set(Object.values(HintTier)) };
  }
  if (totalWords === 0) return state;
  const progress = wordsFound / totalWords;
  const unlocked = new Set<HintTier>();
  for (const tier of Object.values(HintTier)) {
    if (progress >= HINT_TIER_THRESHOLDS[tier]) {
      unlocked.add(tier);
    }
  }
  return { ...state, unlockedTiers: unlocked };
}

export function toggleHintTier(state: HintState, tier: HintTier): HintState {
  const expanded = new Set(state.expandedTiers);
  if (expanded.has(tier)) {
    expanded.delete(tier);
  } else {
    expanded.add(tier);
  }
  return { ...state, expandedTiers: expanded };
}

export interface GameState {
  puzzleId: string;
  foundWords: Set<string>;
  currentScore: number;
  currentRank: Rank;
  hintState: HintState;
}

export function createGameState(puzzleId: string): GameState {
  return {
    puzzleId,
    foundWords: new Set(),
    currentScore: 0,
    currentRank: Rank.BEGINNER,
    hintState: createHintState()
  };
}

export function addFoundWord(
  state: GameState,
  word: string,
  score: number,
  newRank: Rank
): GameState {
  const foundWords = new Set(state.foundWords);
  foundWords.add(word.toLowerCase());
  return {
    ...state,
    foundWords,
    currentScore: state.currentScore + score,
    currentRank: newRank
  };
}
