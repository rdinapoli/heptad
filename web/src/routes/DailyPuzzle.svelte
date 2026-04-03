<script lang="ts">
  import OrbitalDisplay from '../components/OrbitalDisplay.svelte';
  import InputArea from '../components/InputArea.svelte';
  import ScoreDisplay from '../components/ScoreDisplay.svelte';
  import TabBar from '../components/TabBar.svelte';
  import HintsPanel from '../components/HintsPanel.svelte';
  import FoundWordsList from '../components/FoundWordsList.svelte';
  import DefinitionDialog from '../components/DefinitionDialog.svelte';
  import { dailyStore } from '$lib/stores/dailyStore.svelte';
  import { definitionStore } from '$lib/stores/definitionStore.svelte';
  import { calculateWordScore, isPangram as checkPangram } from '$lib/models/puzzle';
  import { RANK_DISPLAY_NAMES } from '$lib/models/rank';
  import { onMount } from 'svelte';

  interface Props {
    onBack: () => void;
  }

  let { onBack }: Props = $props();
  let activeTab = $state(0);
  let showDefinition = $state<string | null>(null);

  onMount(() => {
    dailyStore.load();
    return () => dailyStore.cleanup();
  });

  function handleKeydown(e: KeyboardEvent) {
    if (dailyStore.state.type !== 'playing') return;
    if (showDefinition) return;
    const s = dailyStore.state;
    const allLetters = new Set([...s.puzzle.outerLetters, s.puzzle.centerLetter]);

    if (e.key === 'Enter') {
      e.preventDefault();
      dailyStore.submitWord();
    } else if (e.key === 'Backspace') {
      e.preventDefault();
      dailyStore.deleteLetter();
    } else if (e.key.length === 1 && /^[a-zA-Z]$/.test(e.key) && allLetters.has(e.key.toLowerCase())) {
      e.preventDefault();
      dailyStore.addLetter(e.key.toLowerCase());
    }
  }

  function openDefinition(word: string) {
    showDefinition = word;
    definitionStore.fetch(word);
  }

  function closeDefinition() {
    showDefinition = null;
    definitionStore.clear();
  }
</script>

<svelte:window onkeydown={handleKeydown} />

<div class="screen">
  <div class="top-bar">
    <button class="back-btn" onclick={onBack}>← Back</button>
    <h1>Daily #{dailyStore.state.type === 'playing' ? dailyStore.state.puzzleNumber : ''}</h1>
    <div class="streak-badge">
      {#if dailyStore.state.type === 'playing' && dailyStore.state.currentStreak > 0}
        🔥 {dailyStore.state.currentStreak}
      {/if}
    </div>
  </div>

  {#if dailyStore.state.type === 'loading'}
    <div class="center-content">
      <div class="spinner"></div>
      <p>Loading today's puzzle...</p>
    </div>

  {:else if dailyStore.state.type === 'error'}
    <div class="center-content">
      <p class="error-text">{dailyStore.state.message}</p>
      <button class="btn-primary" onclick={onBack}>Back to Menu</button>
    </div>

  {:else if dailyStore.state.type === 'playing'}
    {@const s = dailyStore.state}
    <ScoreDisplay currentScore={s.gameState.currentScore} maxScore={s.puzzle.maxScore} currentRank={s.gameState.currentRank} />
    <TabBar tabs={['Play', 'Hints', 'Words']} {activeTab} onTabChange={(i) => activeTab = i} />

    <div class="content">
      {#if activeTab === 0}
        <div class="play-tab">
          <OrbitalDisplay
            centerLetter={s.puzzle.centerLetter}
            outerLetters={s.shuffledOuterLetters ?? s.puzzle.outerLetters}
            onLetterClick={(l) => dailyStore.addLetter(l)}
          />
          <InputArea
            currentInput={s.currentInput}
            validationMessage={s.validationMessage}
            centerLetter={s.puzzle.centerLetter}
            onShuffle={() => dailyStore.shuffleLetters()}
            onDelete={() => dailyStore.deleteLetter()}
            onSubmit={() => dailyStore.submitWord()}
          />
        </div>
      {:else if activeTab === 1}
        <HintsPanel
          puzzle={s.puzzle}
          foundWords={s.gameState.foundWords}
          currentScore={s.gameState.currentScore}
          hintState={s.gameState.hintState}
          onToggleTier={(tier) => dailyStore.toggleHintTier(tier)}
          onWordClick={openDefinition}
        />
      {:else}
        <FoundWordsList
          puzzle={s.puzzle}
          foundWords={s.gameState.foundWords}
          onWordClick={openDefinition}
        />
      {/if}
    </div>

    <div class="countdown-bar">
      <span>Next puzzle in {s.timeUntilNext}</span>
      <span class="rank-label">{RANK_DISPLAY_NAMES[s.gameState.currentRank]}</span>
    </div>

    {#if showDefinition}
      <DefinitionDialog
        word={showDefinition}
        isPangram={checkPangram(s.puzzle, showDefinition)}
        score={calculateWordScore(s.puzzle, showDefinition)}
        result={definitionStore.result}
        onDismiss={closeDefinition}
      />
    {/if}
  {/if}
</div>

<style>
  .center-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;
    padding: 32px;
    text-align: center;
  }
  .center-content p { color: var(--on-surface-variant); }
  .error-text { color: var(--error); }

  .streak-badge {
    min-width: 60px;
    text-align: right;
    font-size: 16px;
    font-weight: 600;
  }

  .play-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 0;
  }

  .countdown-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 16px;
    background: var(--surface-variant);
    font-size: 13px;
    color: var(--on-surface-variant);
  }
  .rank-label { font-weight: 600; color: var(--primary); }

  .spinner {
    width: 40px;
    height: 40px;
    border: 4px solid var(--surface-variant);
    border-top-color: var(--primary);
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
  @keyframes spin { to { transform: rotate(360deg); } }
</style>
