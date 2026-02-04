<script lang="ts">
  import { RANK_DISPLAY_NAMES, RANK_THRESHOLDS, getNextRank } from '$lib/models/rank';
  import type { Rank } from '$lib/models/rank';
  import { rankColors } from '$lib/theme/colors';

  interface Props {
    currentScore: number;
    maxScore: number;
    currentRank: Rank;
  }

  let { currentScore, maxScore, currentRank }: Props = $props();

  let percentage = $derived(maxScore > 0 ? currentScore / maxScore : 0);
  let nextRank = $derived(getNextRank(currentRank));
  let nextThreshold = $derived(nextRank ? Math.ceil(RANK_THRESHOLDS[nextRank] * maxScore) : maxScore);
  let pointsToNext = $derived(nextThreshold - currentScore);
</script>

<div class="score-display">
  <div class="rank-info">
    <span class="rank-name" style="color: {rankColors[currentRank]}">{RANK_DISPLAY_NAMES[currentRank]}</span>
    <span class="score">{currentScore} / {maxScore}</span>
  </div>
  <div class="progress-bar">
    <div class="progress-fill" style="width: {Math.min(percentage * 100, 100)}%; background: {rankColors[currentRank]}"></div>
    {#each Object.values(RANK_THRESHOLDS) as threshold}
      {#if threshold > 0 && threshold < 1}
        <div class="threshold-marker" style="left: {threshold * 100}%"></div>
      {/if}
    {/each}
  </div>
  {#if nextRank && pointsToNext > 0}
    <div class="next-rank">{pointsToNext} points to {RANK_DISPLAY_NAMES[nextRank]}</div>
  {/if}
</div>

<style>
  .score-display {
    padding: 8px 16px;
  }

  .rank-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 6px;
  }

  .rank-name {
    font-size: 16px;
    font-weight: 600;
  }

  .score {
    font-size: 14px;
    color: var(--on-surface-variant);
  }

  .progress-bar {
    position: relative;
    height: 6px;
    background: var(--surface-variant);
    border-radius: 3px;
    overflow: visible;
  }

  .progress-fill {
    height: 100%;
    border-radius: 3px;
    transition: width 0.3s ease;
  }

  .threshold-marker {
    position: absolute;
    top: -2px;
    width: 2px;
    height: 10px;
    background: var(--outline);
    opacity: 0.4;
  }

  .next-rank {
    font-size: 12px;
    color: var(--on-surface-variant);
    margin-top: 4px;
    text-align: right;
  }
</style>
