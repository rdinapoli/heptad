import type { DailyPuzzlesAsset, DailyPuzzleEntry, StreakData } from '../models/dailyPuzzle';
import { daysBetween, todayDateString } from '../models/dailyPuzzle';
import { Rank } from '../models/rank';
import * as storage from './storageService';

const BASE = import.meta.env.BASE_URL;
let puzzlesAsset: DailyPuzzlesAsset | null = null;

export async function loadDailyPuzzles(): Promise<DailyPuzzlesAsset> {
  if (puzzlesAsset) return puzzlesAsset;
  const res = await fetch(`${BASE}data/daily_puzzles.json`);
  puzzlesAsset = await res.json();
  return puzzlesAsset!;
}

export function getPuzzleForToday(asset: DailyPuzzlesAsset): { entry: DailyPuzzleEntry; puzzleNumber: number } | null {
  const today = todayDateString();
  const idx = daysBetween(asset.start_date, today);
  if (idx < 0 || idx >= asset.puzzles.length) return null;
  return { entry: asset.puzzles[idx], puzzleNumber: idx + 1 };
}

// Streak
export function getStreakData(): StreakData {
  return {
    currentStreak: storage.getInt('streak_current', 0),
    longestStreak: storage.getInt('streak_longest', 0),
    lastCompletedDate: storage.getItem('streak_last_date'),
    totalDailiesCompleted: storage.getInt('streak_total', 0)
  };
}

export function recordCompletion(completedDate: string): void {
  const streak = getStreakData();
  const lastCompleted = streak.lastCompletedDate;
  let newStreak: number;

  if (!lastCompleted) {
    newStreak = 1;
  } else if (completedDate === lastCompleted) {
    newStreak = streak.currentStreak;
  } else if (daysBetween(lastCompleted, completedDate) === 1) {
    newStreak = streak.currentStreak + 1;
  } else {
    newStreak = 1;
  }

  const newTotal = completedDate !== lastCompleted
    ? streak.totalDailiesCompleted + 1
    : streak.totalDailiesCompleted;

  storage.setInt('streak_current', newStreak);
  storage.setInt('streak_longest', Math.max(streak.longestStreak, newStreak));
  storage.setItem('streak_last_date', completedDate);
  storage.setInt('streak_total', newTotal);
}

// Daily progress
export interface DailyProgress {
  foundWords: string[];
  score: number;
  rank: Rank;
}

export function saveDailyProgress(puzzleId: string, foundWords: Set<string>, score: number, rank: Rank): void {
  storage.setJSON('daily_progress', {
    puzzleId,
    foundWords: [...foundWords],
    score,
    rank
  });
}

export function loadDailyProgress(puzzleId: string): DailyProgress | null {
  const data = storage.getJSON<{ puzzleId: string; foundWords: string[]; score: number; rank: string } | null>('daily_progress', null);
  if (!data || data.puzzleId !== puzzleId) return null;
  return {
    foundWords: data.foundWords,
    score: data.score,
    rank: (data.rank as Rank) ?? Rank.BEGINNER
  };
}
