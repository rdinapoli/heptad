<script lang="ts">
  import type { DefinitionResult } from '$lib/services/definitionService';

  interface Props {
    word: string;
    isPangram: boolean;
    score: number;
    result: DefinitionResult;
    showWord?: boolean;
    onDismiss: () => void;
  }

  let { word, isPangram, score, result, showWord = true, onDismiss }: Props = $props();
</script>

<!-- svelte-ignore a11y_click_events_have_key_events a11y_interactive_supports_focus a11y_no_static_element_interactions a11y_no_noninteractive_element_interactions -->
<div class="dialog-overlay" onclick={onDismiss} role="dialog" aria-modal="true" tabindex="-1">
  <div class="dialog-card" onclick={(e) => e.stopPropagation()} role="document">
    <div class="dialog-header">
      {#if showWord}
        <h2>
          {word.toUpperCase()}
          {#if isPangram}<span class="pangram-star">★</span>{/if}
        </h2>
        <div class="word-stats">
          <span>{word.length} letters</span>
          <span>+{score} points</span>
          {#if isPangram}<span class="pangram-badge">Pangram</span>{/if}
        </div>
      {:else}
        <h2>Definition Hint</h2>
        <div class="word-stats">
          <span>{word.length} letters</span>
        </div>
      {/if}
    </div>

    <div class="dialog-body">
      {#if result.type === 'loading'}
        <div class="loading">Loading definition...</div>
      {:else if result.type === 'notFound'}
        <div class="not-found">No definition available for this word.</div>
      {:else if result.type === 'error'}
        <div class="error">{result.message}</div>
      {:else if result.type === 'success'}
        {#if result.definition.phonetic}
          <div class="phonetic">{result.definition.phonetic}</div>
        {/if}
        {#each result.definition.meanings as meaning, i}
          <div class="meaning">
            <div class="pos">{meaning.partOfSpeech}</div>
            {#each meaning.definitions as def, j}
              <div class="def-entry">
                <span class="def-num">{j + 1}.</span>
                <span>{def.definition}</span>
              </div>
              {#if def.example && showWord}
                <div class="example">"{def.example}"</div>
              {/if}
            {/each}
          </div>
        {/each}
      {/if}
    </div>

    <button class="close-btn" onclick={onDismiss}>Close</button>
  </div>
</div>

<style>
  .dialog-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 100;
    padding: 24px;
  }

  .dialog-card {
    background: var(--surface);
    border-radius: 16px;
    padding: 24px;
    max-width: 400px;
    width: 100%;
    max-height: 70vh;
    overflow-y: auto;
  }

  .dialog-header h2 { font-size: 20px; font-weight: 700; }
  .pangram-star { color: var(--tertiary); margin-left: 4px; }
  .word-stats { display: flex; gap: 12px; font-size: 13px; color: var(--on-surface-variant); margin-top: 4px; }
  .pangram-badge { color: var(--tertiary); font-weight: 600; }

  .dialog-body { margin-top: 16px; }
  .loading, .not-found, .error { color: var(--on-surface-variant); font-size: 14px; padding: 16px 0; }
  .error { color: var(--error); }

  .phonetic { color: var(--on-surface-variant); font-size: 14px; margin-bottom: 12px; }

  .meaning { margin-bottom: 12px; }
  .pos { font-size: 13px; font-weight: 600; color: var(--primary); text-transform: capitalize; margin-bottom: 4px; }
  .def-entry { font-size: 14px; margin-bottom: 4px; display: flex; gap: 4px; }
  .def-num { color: var(--on-surface-variant); min-width: 18px; }
  .example { font-size: 13px; color: var(--on-surface-variant); font-style: italic; margin-left: 22px; margin-bottom: 4px; }

  .close-btn {
    margin-top: 16px;
    width: 100%;
    padding: 12px;
    background: var(--primary);
    color: var(--on-primary);
    border-radius: 24px;
    font-size: 16px;
    font-weight: 600;
  }
</style>
