import type { Puzzle } from '../models/puzzle';
import {
  type GameState, type HintTier,
  createGameState, addFoundWord,
  updateHintUnlocks, toggleHintTier as toggleTier
} from '../models/gameState';
import { rankFromScore } from '../models/rank';
import { validate, calculateScore, getSuccessMessage } from '../domain/wordValidator';
import { generatePuzzle } from '../domain/puzzleGenerator';
import { loadDictionary } from '../services/dictionaryService';
import { settings } from './settingsStore.svelte';

export type GameUiState =
  | { type: 'loading' }
  | { type: 'noPuzzle' }
  | { type: 'generating' }
  | { type: 'playing'; puzzle: Puzzle; gameState: GameState; currentInput: string; validationMessage: string | null; shuffledOuterLetters: string[] | null }
  | { type: 'error'; message: string };

let _state = $state<GameUiState>({ type: 'noPuzzle' });
let _messageTimeout: ReturnType<typeof setTimeout> | null = null;

export const gameStore = {
  get state() { return _state; },

  async generateNewPuzzle() {
    _state = { type: 'generating' };
    try {
      const { dictionaryLevel, includeS, allHintsAvailable } = settings.value;
      const dict = await loadDictionary(dictionaryLevel);
      let commonDict: Set<string> | undefined;
      if (dictionaryLevel !== 60) {
        try { commonDict = await loadDictionary(60); } catch { /* ok */ }
      }
      const puzzle = generatePuzzle(dict, dictionaryLevel, includeS, commonDict);
      if (!puzzle) {
        _state = { type: 'error', message: 'Failed to generate puzzle. Please try again.' };
        return;
      }
      let gs = createGameState(puzzle.id);
      gs = { ...gs, hintState: updateHintUnlocks(gs.hintState, 0, puzzle.validWords.size, allHintsAvailable) };
      _state = { type: 'playing', puzzle, gameState: gs, currentInput: '', validationMessage: null, shuffledOuterLetters: null };
    } catch (e: any) {
      _state = { type: 'error', message: e.message ?? 'Unknown error' };
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
      _state = { ...s, gameState: gs, currentInput: '', validationMessage: msg };
      clearMessageAfterDelay();
    } else {
      const { validationMessage: _vm, ...rest } = await_import_workaround(result);
      _state = { ...s, currentInput: '', validationMessage: _vm };
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
  }
};

function await_import_workaround(result: { type: string }) {
  const messages: Record<string, string> = {
    tooShort: 'Too short — words must be 4+ letters',
    missingCenter: 'Missing center letter',
    invalidLetters: 'Uses letters not in the puzzle',
    notInWordList: 'Not in word list',
    alreadyFound: 'Already found'
  };
  return { validationMessage: messages[result.type] ?? '' };
}

function clearMessageAfterDelay() {
  if (_messageTimeout) clearTimeout(_messageTimeout);
  _messageTimeout = setTimeout(() => {
    if (_state.type === 'playing') {
      _state = { ..._state, validationMessage: null };
    }
  }, 2000);
}
