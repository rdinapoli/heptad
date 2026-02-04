import { Rank } from './rank';
import { createPuzzle, type Puzzle } from './puzzle';

export interface DailyPuzzlesAsset {
  version: number;
  start_date: string;
  puzzles: DailyPuzzleEntry[];
}

export interface DailyPuzzleEntry {
  puzzle_number: number;
  center_letter: string;
  outer_letters: string[];
  valid_words: string[];
  pangrams: string[];
  max_score: number;
}

export function dailyEntryToPuzzle(entry: DailyPuzzleEntry): Puzzle {
  return createPuzzle({
    id: `daily-${entry.puzzle_number}`,
    centerLetter: entry.center_letter,
    outerLetters: entry.outer_letters,
    validWords: entry.valid_words,
    pangrams: entry.pangrams,
    maxScore: entry.max_score,
    dictionaryLevel: 70,
    includesS: entry.outer_letters.some(l => l.toLowerCase() === 's') ||
               entry.center_letter.toLowerCase() === 's'
  });
}

export interface StreakData {
  currentStreak: number;
  longestStreak: number;
  lastCompletedDate: string | null;
  totalDailiesCompleted: number;
}

export function isStreakActive(streak: StreakData, today: string): boolean {
  if (!streak.lastCompletedDate) return false;
  const daysSince = daysBetween(streak.lastCompletedDate, today);
  return daysSince <= 1;
}

export function displayStreak(streak: StreakData, today: string): number {
  return isStreakActive(streak, today) ? streak.currentStreak : 0;
}

export function daysBetween(dateA: string, dateB: string): number {
  const a = new Date(dateA + 'T00:00:00');
  const b = new Date(dateB + 'T00:00:00');
  return Math.round((b.getTime() - a.getTime()) / 86400000);
}

export function todayDateString(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

export interface DailyCompletionStats {
  wordsFound: number;
  totalWords: number;
  scoreAchieved: number;
  maxScore: number;
  rankAchieved: Rank;
  pangramsFound: number;
  totalPangrams: number;
}
