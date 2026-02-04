export enum Rank {
  BEGINNER = 'BEGINNER',
  GOOD_START = 'GOOD_START',
  MOVING_UP = 'MOVING_UP',
  GOOD = 'GOOD',
  SOLID = 'SOLID',
  NICE = 'NICE',
  GREAT = 'GREAT',
  AMAZING = 'AMAZING',
  GENIUS = 'GENIUS',
  QUEEN_BEE = 'QUEEN_BEE'
}

export const RANK_THRESHOLDS: Record<Rank, number> = {
  [Rank.BEGINNER]: 0.0,
  [Rank.GOOD_START]: 0.05,
  [Rank.MOVING_UP]: 0.10,
  [Rank.GOOD]: 0.20,
  [Rank.SOLID]: 0.30,
  [Rank.NICE]: 0.40,
  [Rank.GREAT]: 0.50,
  [Rank.AMAZING]: 0.60,
  [Rank.GENIUS]: 0.70,
  [Rank.QUEEN_BEE]: 1.0
};

export const RANK_DISPLAY_NAMES: Record<Rank, string> = {
  [Rank.BEGINNER]: 'Beginner',
  [Rank.GOOD_START]: 'Good Start',
  [Rank.MOVING_UP]: 'Moving Up',
  [Rank.GOOD]: 'Good',
  [Rank.SOLID]: 'Solid',
  [Rank.NICE]: 'Nice',
  [Rank.GREAT]: 'Great',
  [Rank.AMAZING]: 'Amazing',
  [Rank.GENIUS]: 'Genius',
  [Rank.QUEEN_BEE]: 'Queen Bee'
};

const RANK_ORDER = Object.values(Rank);

export function rankFromScore(currentScore: number, maxScore: number): Rank {
  if (maxScore === 0) return Rank.BEGINNER;
  const percentage = currentScore / maxScore;
  let result = Rank.BEGINNER;
  for (const rank of RANK_ORDER) {
    if (percentage >= RANK_THRESHOLDS[rank]) {
      result = rank;
    }
  }
  return result;
}

export function getNextRank(currentRank: Rank): Rank | null {
  const idx = RANK_ORDER.indexOf(currentRank);
  return idx < RANK_ORDER.length - 1 ? RANK_ORDER[idx + 1] : null;
}

export function rankOrdinal(rank: Rank): number {
  return RANK_ORDER.indexOf(rank);
}
