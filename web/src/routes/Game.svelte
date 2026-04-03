<script lang="ts">
  import OrbitalDisplay from '../components/OrbitalDisplay.svelte';
  import InputArea from '../components/InputArea.svelte';
  import ScoreDisplay from '../components/ScoreDisplay.svelte';
  import TabBar from '../components/TabBar.svelte';
  import HintsPanel from '../components/HintsPanel.svelte';
  import FoundWordsList from '../components/FoundWordsList.svelte';
  import DefinitionDialog from '../components/DefinitionDialog.svelte';
  import { gameStore } from '$lib/stores/gameStore.svelte';
  import { definitionStore } from '$lib/stores/definitionStore.svelte';
  import { calculateWordScore, isPangram as checkPangram } from '$lib/models/puzzle';

  interface Props {
    onBack: () => void;
  }

  let { onBack }: Props = $props();
  let activeTab = $state(0);
  let showDefinition = $state<string | null>(null);

  function handleKeydown(e: KeyboardEvent) {
    if (gameStore.state.type !== 'playing') return;
    if (showDefinition) return;
    const s = gameStore.state;
    const allLetters = new Set([...s.puzzle.outerLetters, s.puzzle.centerLetter]);

    if (e.key === 'Enter') {
      e.preventDefault();
      gameStore.submitWord();
    } else if (e.key === 'Backspace') {
      e.preventDefault();
      gameStore.deleteLetter();
    } else if (e.key.length === 1 && /^[a-zA-Z]$/.test(e.key) && allLetters.has(e.key.toLowerCase())) {
      e.preventDefault();
      gameStore.addLetter(e.key.toLowerCase());
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
    <h1>Heptad</h1>
    <div style="width: 60px"></div>
  </div>

  {#if gameStore.state.type === 'noPuzzle'}
    <div class="center-content">
      <h2>Random Puzzle</h2>
      <p>Generate a new puzzle with random letters.</p>
      <button class="btn-primary" onclick={() => gameStore.generateNewPuzzle()}>New Puzzle</button>
    </div>

  {:else if gameStore.state.type === 'generating'}
    <div class="center-content">
      <div class="spinner"></div>
      <p>Generating puzzle...</p>
    </div>

  {:else if gameStore.state.type === 'error'}
    <div class="center-content">
      <p class="error-text">{gameStore.state.message}</p>
      <button class="btn-primary" onclick={() => gameStore.generateNewPuzzle()}>Try Again</button>
    </div>

  {:else if gameStore.state.type === 'playing'}
    {@const s = gameStore.state}
    <ScoreDisplay currentScore={s.gameState.currentScore} maxScore={s.puzzle.maxScore} currentRank={s.gameState.currentRank} />
    <TabBar tabs={['Play', 'Hints', 'Words']} {activeTab} onTabChange={(i) => activeTab = i} />

    <div class="content">
      {#if activeTab === 0}
        <div class="play-tab">
          <OrbitalDisplay
            centerLetter={s.puzzle.centerLetter}
            outerLetters={s.shuffledOuterLetters ?? s.puzzle.outerLetters}
            onLetterClick={(l) => gameStore.addLetter(l)}
          />
          <InputArea
            currentInput={s.currentInput}
            validationMessage={s.validationMessage}
            centerLetter={s.puzzle.centerLetter}
            onShuffle={() => gameStore.shuffleLetters()}
            onDelete={() => gameStore.deleteLetter()}
            onSubmit={() => gameStore.submitWord()}
          />
        </div>
      {:else if activeTab === 1}
        <HintsPanel
          puzzle={s.puzzle}
          foundWords={s.gameState.foundWords}
          currentScore={s.gameState.currentScore}
          hintState={s.gameState.hintState}
          onToggleTier={(tier) => gameStore.toggleHintTier(tier)}
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

    <div class="bottom-actions">
      <button class="btn-outline" onclick={() => gameStore.generateNewPuzzle()}>New Puzzle</button>
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
  .center-content h2 { font-size: 24px; }
  .center-content p { color: var(--on-surface-variant); }
  .error-text { color: var(--error); }

  .play-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 16px 0;
  }

  .bottom-actions {
    padding: 12px 16px;
    display: flex;
    justify-content: center;
    border-top: 1px solid var(--surface-variant);
  }

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
