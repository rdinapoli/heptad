<script lang="ts">
  import { getStreakData, loadDailyProgress, loadDailyPuzzles, getPuzzleForToday } from '$lib/services/dailyPuzzleService';
  import { displayStreak, todayDateString, dailyEntryToPuzzle } from '$lib/models/dailyPuzzle';
  import { RANK_DISPLAY_NAMES } from '$lib/models/rank';
  import { onMount } from 'svelte';

  interface Props {
    onNavigate: (route: string) => void;
  }

  let { onNavigate }: Props = $props();

  let streak = $state(0);
  let dailyStatus = $state<string>('');
  let dailySubtext = $state('');

  onMount(async () => {
    const streakData = getStreakData();
    const today = todayDateString();
    streak = displayStreak(streakData, today);

    try {
      const asset = await loadDailyPuzzles();
      const result = getPuzzleForToday(asset);
      if (result) {
        const puzzle = dailyEntryToPuzzle(result.entry);
        const progress = loadDailyProgress(puzzle.id);
        if (progress && progress.foundWords.length > 0) {
          dailyStatus = `${progress.foundWords.length} / ${puzzle.validWords.size} words`;
          dailySubtext = RANK_DISPLAY_NAMES[progress.rank];
        } else {
          dailyStatus = 'Not started';
          dailySubtext = `${puzzle.validWords.size} words to find`;
        }
      } else {
        dailyStatus = 'No puzzle today';
      }
    } catch {
      dailyStatus = 'Tap to play';
    }
  });
</script>

<div class="screen">
  <div class="menu-header">
    <h1 class="title">Heptad</h1>
    <p class="subtitle">Find words, earn ranks, have fun.</p>
  </div>

  {#if streak > 0}
    <div class="streak-banner">
      <span class="fire">🔥</span>
      <span>{streak} day streak!</span>
    </div>
  {/if}

  <div class="menu-cards">
    <button class="menu-card daily" onclick={() => onNavigate('daily')}>
      <div class="card-icon">📅</div>
      <div class="card-content">
        <h2>Daily Puzzle</h2>
        <p class="card-status">{dailyStatus}</p>
        {#if dailySubtext}
          <p class="card-sub">{dailySubtext}</p>
        {/if}
      </div>
      <span class="card-arrow">→</span>
    </button>

    <button class="menu-card random" onclick={() => onNavigate('game')}>
      <div class="card-icon">🎲</div>
      <div class="card-content">
        <h2>Random Puzzle</h2>
        <p class="card-status">Generate a new puzzle</p>
      </div>
      <span class="card-arrow">→</span>
    </button>

    <button class="menu-card settings" onclick={() => onNavigate('settings')}>
      <div class="card-icon">⚙️</div>
      <div class="card-content">
        <h2>Settings</h2>
        <p class="card-status">Dictionary, theme, hints</p>
      </div>
      <span class="card-arrow">→</span>
    </button>
  </div>
</div>

<style>
  .menu-header {
    text-align: center;
    padding: 48px 16px 24px;
  }
  .title {
    font-size: 36px;
    font-weight: 800;
    color: var(--primary);
    letter-spacing: -1px;
  }
  .subtitle {
    font-size: 16px;
    color: var(--on-surface-variant);
    margin-top: 4px;
  }

  .streak-banner {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    padding: 10px;
    margin: 0 24px 16px;
    background: var(--tertiary-container);
    color: var(--on-tertiary-container);
    border-radius: 12px;
    font-weight: 600;
    font-size: 16px;
  }
  .fire { font-size: 20px; }

  .menu-cards {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 0 20px;
  }

  .menu-card {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: var(--surface);
    border: 1px solid var(--outline);
    border-radius: 16px;
    text-align: left;
    color: var(--on-surface);
    transition: transform 0.1s;
  }
  .menu-card:active { transform: scale(0.98); }

  .card-icon { font-size: 32px; }
  .card-content { flex: 1; }
  .card-content h2 { font-size: 18px; font-weight: 600; }
  .card-status { font-size: 14px; color: var(--on-surface-variant); margin-top: 2px; }
  .card-sub { font-size: 13px; color: var(--primary); margin-top: 2px; font-weight: 500; }
  .card-arrow { font-size: 20px; color: var(--on-surface-variant); }
</style>
