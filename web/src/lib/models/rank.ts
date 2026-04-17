export enum Rank {
  ADRIFT = 'ADRIFT',
  TELLURIC = 'TELLURIC',
  ORBITAL = 'ORBITAL',
  SELENIAN = 'SELENIAN',
  COMETARY = 'COMETARY',
  METEORIC = 'METEORIC',
  STELLAR = 'STELLAR',
  NEBULAR = 'NEBULAR',
  GALACTIC = 'GALACTIC',
  UNIVERSAL = 'UNIVERSAL'
}

export const RANK_THRESHOLDS: Record<Rank, number> = {
  [Rank.ADRIFT]: 0.0,
  [Rank.TELLURIC]: 0.05,
  [Rank.ORBITAL]: 0.10,
  [Rank.SELENIAN]: 0.20,
  [Rank.COMETARY]: 0.30,
  [Rank.METEORIC]: 0.40,
  [Rank.STELLAR]: 0.55,
  [Rank.NEBULAR]: 0.70,
  [Rank.GALACTIC]: 0.85,
  [Rank.UNIVERSAL]: 1.0
};

export const RANK_DISPLAY_NAMES: Record<Rank, string> = {
  [Rank.ADRIFT]: 'Adrift',
  [Rank.TELLURIC]: 'Telluric',
  [Rank.ORBITAL]: 'Orbital',
  [Rank.SELENIAN]: 'Selenian',
  [Rank.COMETARY]: 'Cometary',
  [Rank.METEORIC]: 'Meteoric',
  [Rank.STELLAR]: 'Stellar',
  [Rank.NEBULAR]: 'Nebular',
  [Rank.GALACTIC]: 'Galactic',
  [Rank.UNIVERSAL]: 'Universal'
};

const RANK_ORDER = Object.values(Rank);
const VALID_RANKS = new Set<string>(RANK_ORDER);

export function rankFromScore(currentScore: number, maxScore: number): Rank {
  if (maxScore === 0) return Rank.ADRIFT;
  const percentage = currentScore / maxScore;
  let result = Rank.ADRIFT;
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

export function isValidRank(value: string): value is Rank {
  return VALID_RANKS.has(value);
}
