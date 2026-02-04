import type { Puzzle } from '../models/puzzle';
import { dailyEntryToPuzzle, todayDateString, displayStreak } from '../models/dailyPuzzle';
import {
  type GameState, type HintTier,
  createGameState, addFoundWord,
  updateHintUnlocks, toggleHintTier as toggleTier
} from '../models/gameState';
import { Rank, rankFromScore } from '../models/rank';
import { validate, calculateScore, getSuccessMessage } from '../domain/wordValidator';
import {
  loadDailyPuzzles, getPuzzleForToday,
  getStreakData, recordCompletion,
  saveDailyProgress, loadDailyProgress
} from '../services/dailyPuzzleService';
import { settings } from './settingsStore.svelte';

export type DailyUiState =
  | { type: 'loading' }
  | { type: 'playing'; puzzle: Puzzle; gameState: GameState; currentInput: string; validationMessage: string | null; shuffledOuterLetters: string[] | null; puzzleNumber: number; currentStreak: number; timeUntilNext: string; showCompletionDialog: boolean }
  | { type: 'error'; message: string };

let _state = $state<DailyUiState>({ type: 'loading' });
let _countdownInterval: ReturnType<typeof setInterval> | null = null;
let _messageTimeout: ReturnType<typeof setTimeout> | null = null;

function computeCountdown(): string {
  const now = new Date();
  const midnight = new Date(now);
  midnight.setDate(midnight.getDate() + 1);
  midnight.setHours(0, 0, 0, 0);
  const diff = midnight.getTime() - now.getTime();
  const h = Math.floor(diff / 3600000);
  const m = Math.floor((diff % 3600000) / 60000);
  const s = Math.floor((diff % 60000) / 1000);
  return `${h}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export const dailyStore = {
  get state() { return _state; },

  async load() {
    _state = { type: 'loading' };
    try {
      const asset = await loadDailyPuzzles();
      const result = getPuzzleForToday(asset);
      if (!result) {
        _state = { type: 'error', message: 'No puzzle available for today.' };
        return;
      }
      const puzzle = dailyEntryToPuzzle(result.entry);
      let gs = createGameState(puzzle.id);

      // Restore progress
      const saved = loadDailyProgress(puzzle.id);
      if (saved) {
        gs = {
          ...gs,
          foundWords: new Set(saved.foundWords),
          currentScore: saved.score,
          currentRank: saved.rank
        };
      }

      gs = { ...gs, hintState: updateHintUnlocks(gs.hintState, gs.foundWords.size, puzzle.validWords.size, settings.value.allHintsAvailable) };

      const streak = getStreakData();
      const today = todayDateString();

      _state = {
        type: 'playing',
        puzzle,
        gameState: gs,
        currentInput: '',
        validationMessage: null,
        shuffledOuterLetters: null,
        puzzleNumber: result.puzzleNumber,
        currentStreak: displayStreak(streak, today),
        timeUntilNext: computeCountdown(),
        showCompletionDialog: false
      };

      startCountdown();
    } catch (e: any) {
      _state = { type: 'error', message: e.message ?? 'Failed to load daily puzzle' };
    }
  },

  addLetter(letter: string) {
    if (_state.type !== 'playing') return;
    _state = { ..._state, currentInput: _state.currentInput + letter.toLowerCase(), validationMessage: null };
  },

  deleteLetter() {
    if (_state.type !== 'playing' || _state.currentInput.length === 0) return;
    _state = { ..._state, currentInput: _state.currentInput.slice(0, -1), validationMessage: null };
  },

  clearInput() {
    if (_state.type !== 'playing') return;
    _state = { ..._state, currentInput: '', validationMessage: null };
  },

  submitWord() {
    if (_state.type !== 'playing') return;
    const s = _state;
    const word = s.currentInput.toLowerCase().trim();
    if (!word) return;

    const result = validate(s.puzzle, word, s.gameState.foundWords);
    if (result.type === 'valid') {
      const score = calculateScore(s.puzzle, word);
      const newScore = s.gameState.currentScore + score;
      const newRank = rankFromScore(newScore, s.puzzle.maxScore);
      let gs = addFoundWord(s.gameState, word, score, newRank);
      gs = { ...gs, hintState: updateHintUnlocks(gs.hintState, gs.foundWords.size, s.puzzle.validWords.size, settings.value.allHintsAvailable) };
      const msg = getSuccessMessage(s.puzzle, word);

      // Save progress
      saveDailyProgress(s.puzzle.id, gs.foundWords, gs.currentScore, gs.currentRank);

      // Record completion for streak
      recordCompletion(todayDateString());
      const streak = getStreakData();
      const today = todayDateString();

      _state = {
        ...s,
        gameState: gs,
        currentInput: '',
        validationMessage: msg,
        currentStreak: displayStreak(streak, today),
        showCompletionDialog: false
      };
      clearMessageAfterDelay();
    } else {
      const messages: Record<string, string> = {
        tooShort: 'Too short — words must be 4+ letters',
        missingCenter: 'Missing center letter',
        invalidLetters: 'Uses letters not in the puzzle',
        notInWordList: 'Not in word list',
        alreadyFound: 'Already found'
      };
      _state = { ...s, currentInput: '', validationMessage: messages[result.type] ?? '' };
      clearMessageAfterDelay();
    }
  },

  shuffleLetters() {
    if (_state.type !== 'playing') return;
    const current = _state.shuffledOuterLetters ?? _state.puzzle.outerLetters;
    const shuffled = [...current].sort(() => Math.random() - 0.5);
    _state = { ..._state, shuffledOuterLetters: shuffled };
  },

  toggleHintTier(tier: HintTier) {
    if (_state.type !== 'playing') return;
    _state = { ..._state, gameState: { ..._state.gameState, hintState: toggleTier(_state.gameState.hintState, tier) } };
  },

  dismissCompletionDialog() {
    if (_state.type !== 'playing') return;
    _state = { ..._state, showCompletionDialog: false };
  },

  cleanup() {
    if (_countdownInterval) {
      clearInterval(_countdownInterval);
      _countdownInterval = null;
    }
  }
};

function startCountdown() {
  if (_countdownInterval) clearInterval(_countdownInterval);
  _countdownInterval = setInterval(() => {
    if (_state.type === 'playing') {
      _state = { ..._state, timeUntilNext: computeCountdown() };
    }
  }, 1000);
}

function clearMessageAfterDelay() {
  if (_messageTimeout) clearTimeout(_messageTimeout);
  _messageTimeout = setTimeout(() => {
    if (_state.type === 'playing') {
      _state = { ..._state, validationMessage: null };
    }
  }, 2000);
}
