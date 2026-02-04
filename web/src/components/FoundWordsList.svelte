<script lang="ts">
  import type { Puzzle } from '$lib/models/puzzle';
  import { calculateWordScore } from '$lib/models/puzzle';

  interface Props {
    puzzle: Puzzle;
    foundWords: Set<string>;
    onWordClick?: (word: string) => void;
  }

  let { puzzle, foundWords, onWordClick }: Props = $props();

  let sortedWords = $derived(
    [...foundWords].sort((a, b) => a.localeCompare(b))
  );
</script>

<div class="found-words">
  <div class="header">
    <span>Found Words</span>
    <span class="count">{foundWords.size} / {puzzle.validWords.size}</span>
  </div>

  {#if sortedWords.length === 0}
    <div class="empty">No words found yet. Start typing!</div>
  {:else}
    <div class="word-list">
      {#each sortedWords as word}
        {@const isPangram = puzzle.pangrams.has(word)}
        {@const score = calculateWordScore(puzzle, word)}
        <button
          class="word-item"
          class:pangram={isPangram}
          onclick={() => onWordClick?.(word)}
        >
          <span class="word-text">{word.toUpperCase()}</span>
          <span class="word-meta">
            {#if isPangram}<span class="star">★</span>{/if}
            <span class="points">+{score}</span>
          </span>
        </button>
      {/each}
    </div>
  {/if}
</div>

<style>
  .found-words {
    padding: 16px;
    height: 100%;
    display: flex;
    flex-direction: column;
  }

  .header {
    display: flex;
    justify-content: space-between;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .count { color: var(--on-surface-variant); font-weight: 400; }

  .empty {
    color: var(--on-surface-variant);
    text-align: center;
    padding: 32px 16px;
    font-size: 14px;
  }

  .word-list {
    display: flex;
    flex-direction: column;
    gap: 2px;
    overflow-y: auto;
  }

  .word-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    background: transparent;
    border-radius: 8px;
    color: var(--on-surface);
    font-size: 15px;
    text-align: left;
    transition: background 0.1s;
  }
  .word-item:active { background: var(--surface-variant); }
  .word-item.pangram { background: var(--tertiary-container); color: var(--on-tertiary-container); }

  .word-meta {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: var(--on-surface-variant);
  }

  .star { color: var(--tertiary); font-size: 16px; }
  .points { font-weight: 500; }
</style>
