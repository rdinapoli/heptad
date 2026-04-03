<script lang="ts">
  import type { Puzzle } from '$lib/models/puzzle';
  import { HintTier, HINT_TIER_NAMES, HINT_TIER_THRESHOLDS, type HintState } from '$lib/models/gameState';
  import {
    generateTwoLetterHints, generateGridHints, generateLetterRevealHints,
    getWordLengths, getStartingLetters, getProgressPercentage,
    type TwoLetterHint, type GridCell, type LetterRevealHint
  } from '$lib/domain/hintGenerator';

  interface Props {
    puzzle: Puzzle;
    foundWords: Set<string>;
    currentScore: number;
    hintState: HintState;
    onToggleTier: (tier: HintTier) => void;
    onWordClick?: (word: string) => void;
  }

  let { puzzle, foundWords, currentScore, hintState, onToggleTier, onWordClick }: Props = $props();

  let progress = $derived(getProgressPercentage(puzzle, currentScore));
  let twoLetterHints = $derived(generateTwoLetterHints(puzzle, foundWords));
  let gridCells = $derived(generateGridHints(puzzle, foundWords));
  let wordLengths = $derived(getWordLengths(puzzle));
  let startingLetters = $derived(getStartingLetters(puzzle));
  let letterRevealHints = $derived(generateLetterRevealHints(puzzle, foundWords));
  let isDefUnlocked = $derived(hintState.unlockedTiers.has(HintTier.DEFINITION_HINT));
  let remainingCount = $derived(puzzle.validWords.size - foundWords.size);

  const tiers = [HintTier.TWO_LETTER_LIST, HintTier.GRID_VIEW, HintTier.LETTER_REVEAL, HintTier.DEFINITION_HINT];
</script>

<div class="hints-panel">
  <div class="progress-header">
    <div class="progress-row">
      <span>Progress</span>
      <span class="progress-text">{currentScore} / {puzzle.maxScore} points ({Math.round(progress * 100)}%)</span>
    </div>
    <div class="progress-bar">
      <div class="progress-fill" style="width: {progress * 100}%"></div>
    </div>
  </div>

  {#each tiers as tier}
    {@const unlocked = hintState.unlockedTiers.has(tier)}
    {@const expanded = hintState.expandedTiers.has(tier)}
    <div class="tier-card" class:locked={!unlocked}>
      <button class="tier-header" onclick={() => unlocked && onToggleTier(tier)} disabled={!unlocked}>
        <div class="tier-info">
          {#if !unlocked}<span class="lock-icon">🔒</span>{/if}
          <div>
            <div class="tier-name">{HINT_TIER_NAMES[tier]}</div>
            {#if !unlocked}
              <div class="unlock-text">Unlocks at {Math.round(HINT_TIER_THRESHOLDS[tier] * 100)}%</div>
            {/if}
          </div>
        </div>
        {#if unlocked}
          <span class="expand-icon">{expanded ? '▲' : '▼'}</span>
        {/if}
      </button>

      {#if unlocked && expanded}
        <div class="tier-content">
          {#if tier === HintTier.TWO_LETTER_LIST}
            <div class="two-letter-hints">
              {#each Object.entries(groupByFirstLetter(twoLetterHints)) as [, hints]}
                <div class="hint-row">
                  {#each hints as hint}
                    <span class="hint-chip" class:complete={hint.foundCount >= hint.totalCount} class:partial={hint.foundCount > 0 && hint.foundCount < hint.totalCount}>
                      <strong>{hint.prefix}</strong>
                      {hint.foundCount >= hint.totalCount ? '✓' : `${hint.foundCount}/${hint.totalCount}`}
                    </span>
                  {/each}
                </div>
              {/each}
            </div>

          {:else if tier === HintTier.GRID_VIEW}
            <div class="grid-view">
              <div class="grid-row">
                <div class="grid-cell header"></div>
                {#each wordLengths as len}
                  <div class="grid-cell header">{len}</div>
                {/each}
              </div>
              {#each startingLetters as letter}
                <div class="grid-row">
                  <div class="grid-cell header">{letter}</div>
                  {#each wordLengths as len}
                    {@const cell = gridCells.find(c => c.letter === letter && c.length === len)}
                    <div class="grid-cell" class:complete={cell?.foundCount === cell?.totalCount && cell != null} class:partial={cell != null && cell.foundCount > 0 && cell.foundCount < cell.totalCount} class:has-words={cell != null}>
                      {#if cell}
                        {cell.foundCount >= cell.totalCount ? '✓' : cell.totalCount - cell.foundCount}
                      {/if}
                    </div>
                  {/each}
                </div>
              {/each}
            </div>

          {:else if tier === HintTier.LETTER_REVEAL}
            {#if letterRevealHints.length === 0}
              <div class="all-found">All words found!</div>
            {:else}
              <div class="reveal-info">{letterRevealHints.length} words remaining</div>
              {#if isDefUnlocked}
                <div class="tap-info">Tap a word to see its definition</div>
              {/if}
              <div class="reveal-list">
                {#each Object.entries(groupRevealByPrefix(letterRevealHints)) as [prefix, hints]}
                  <div class="reveal-row">
                    <span class="reveal-prefix">{prefix}:</span>
                    <div class="reveal-words">
                      {#each hints as hint}
                        {#if isDefUnlocked && onWordClick}
                          <button class="reveal-word clickable" class:pangram={hint.isPangram} onclick={() => onWordClick?.(hint.fullWord)}>
                            {hint.isPangram ? `[${hint.partialWord}]` : hint.partialWord}
                          </button>
                        {:else}
                          <span class="reveal-word" class:pangram={hint.isPangram}>
                            {hint.isPangram ? `[${hint.partialWord}]` : hint.partialWord}
                          </span>
                        {/if}
                      {/each}
                    </div>
                  </div>
                {/each}
                <div class="pangram-legend">[brackets] = pangram</div>
              </div>
            {/if}

          {:else if tier === HintTier.DEFINITION_HINT}
            {#if remainingCount === 0}
              <div class="all-found">All words found!</div>
            {:else if isDefUnlocked}
              <p>You can now tap on words in the Letter Reveal hint above to see their definitions!</p>
              <p class="remaining">{remainingCount} words remaining to find.</p>
            {:else}
              <p>Unlock this tier to tap on unfound words and see their definitions.</p>
              <p class="remaining">{remainingCount} words remaining to find.</p>
            {/if}
          {/if}
        </div>
      {/if}
    </div>
  {/each}
</div>

<script lang="ts" module>
  function groupByFirstLetter(hints: TwoLetterHint[]): Record<string, TwoLetterHint[]> {
    const groups: Record<string, TwoLetterHint[]> = {};
    for (const h of hints) {
      const key = h.prefix[0];
      (groups[key] ??= []).push(h);
    }
    return groups;
  }

  function groupRevealByPrefix(hints: LetterRevealHint[]): Record<string, LetterRevealHint[]> {
    const groups: Record<string, LetterRevealHint[]> = {};
    for (const h of hints) {
      const key = h.partialWord.slice(0, 2);
      (groups[key] ??= []).push(h);
    }
    return groups;
  }
</script>

<style>
  .hints-panel { padding: 16px; display: flex; flex-direction: column; gap: 12px; }

  .progress-header { margin-bottom: 8px; }
  .progress-row { display: flex; justify-content: space-between; margin-bottom: 6px; font-size: 14px; }
  .progress-text { color: var(--on-surface-variant); }
  .progress-bar { height: 8px; background: var(--surface-variant); border-radius: 4px; overflow: hidden; }
  .progress-fill { height: 100%; background: var(--primary); border-radius: 4px; transition: width 0.3s; }

  .tier-card { background: var(--surface); border: 1px solid var(--outline); border-radius: 12px; overflow: hidden; }
  .tier-card.locked { opacity: 0.7; }

  .tier-header { display: flex; justify-content: space-between; align-items: center; width: 100%; padding: 14px 16px; background: transparent; color: var(--on-surface); text-align: left; }
  .tier-header:disabled { cursor: default; }

  .tier-info { display: flex; align-items: center; gap: 10px; }
  .lock-icon { font-size: 16px; }
  .tier-name { font-size: 14px; font-weight: 600; }
  .unlock-text { font-size: 12px; color: var(--on-surface-variant); }
  .expand-icon { font-size: 12px; color: var(--on-surface-variant); }

  .tier-content { padding: 0 16px 16px; }

  /* Two-letter hints */
  .two-letter-hints { display: flex; flex-direction: column; gap: 6px; }
  .hint-row { display: flex; flex-wrap: wrap; gap: 6px; }
  .hint-chip { display: inline-flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: 8px; font-size: 13px; background: var(--surface-variant); }
  .hint-chip.complete { background: var(--primary-container); }
  .hint-chip.partial { background: var(--secondary-container); }

  /* Grid view */
  .grid-view { overflow-x: auto; }
  .grid-row { display: flex; }
  .grid-cell { width: 38px; height: 38px; display: flex; align-items: center; justify-content: center; font-size: 12px; border-radius: 4px; margin: 1px; }
  .grid-cell.header { font-weight: 700; }
  .grid-cell.has-words { background: var(--surface-variant); border: 1px solid var(--outline); }
  .grid-cell.complete { background: var(--primary-container); }
  .grid-cell.partial { background: var(--secondary-container); }

  /* Letter reveal */
  .reveal-info { font-size: 13px; color: var(--on-surface-variant); margin-bottom: 8px; }
  .tap-info { font-size: 13px; color: var(--primary); margin-bottom: 6px; }
  .reveal-list { display: flex; flex-direction: column; gap: 4px; }
  .reveal-row { display: flex; gap: 8px; align-items: flex-start; }
  .reveal-prefix { font-weight: 700; font-size: 13px; min-width: 36px; }
  .reveal-words { display: flex; flex-wrap: wrap; gap: 6px; }
  .reveal-word { font-family: monospace; font-size: 13px; }
  .reveal-word.pangram { font-weight: 700; }
  .reveal-word.clickable { background: transparent; color: var(--primary); cursor: pointer; padding: 2px 4px; border-radius: 4px; }
  .reveal-word.clickable:hover { background: var(--primary-container); }
  .pangram-legend { font-size: 12px; color: var(--on-surface-variant); margin-top: 8px; }
  .all-found { color: var(--primary); font-weight: 500; }
  .remaining { font-size: 13px; color: var(--on-surface-variant); margin-top: 4px; }
</style>
