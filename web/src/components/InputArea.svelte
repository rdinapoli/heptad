<script lang="ts">
  interface Props {
    currentInput: string;
    validationMessage: string | null;
    centerLetter: string;
    onShuffle: () => void;
    onDelete: () => void;
    onSubmit: () => void;
  }

  let { currentInput, validationMessage, centerLetter, onShuffle, onDelete, onSubmit }: Props = $props();

  let isError = $derived(
    validationMessage !== null &&
    !validationMessage.startsWith('+') &&
    !validationMessage.startsWith('PANGRAM')
  );
  let isPangram = $derived(validationMessage?.startsWith('PANGRAM') ?? false);
  let isSuccess = $derived(
    validationMessage !== null && !isError
  );
</script>

<div class="input-area">
  <div class="word-display" class:error={isError} class:success={isSuccess} class:pangram={isPangram}>
    {#if currentInput}
      {#each currentInput.split('') as ch}
        <span class:center={ch === centerLetter}>{ch.toUpperCase()}</span>
      {/each}
    {:else if validationMessage}
      <span class="message" class:error-msg={isError} class:success-msg={isSuccess} class:pangram-msg={isPangram}>
        {validationMessage}
      </span>
    {:else}
      <span class="placeholder">Type or tap letters</span>
    {/if}
  </div>

  <div class="action-buttons">
    <button class="action-btn" onclick={onShuffle} title="Shuffle">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <polyline points="16 3 21 3 21 8"></polyline>
        <line x1="4" y1="20" x2="21" y2="3"></line>
        <polyline points="21 16 21 21 16 21"></polyline>
        <line x1="15" y1="15" x2="21" y2="21"></line>
        <line x1="4" y1="4" x2="9" y2="9"></line>
      </svg>
    </button>

    <button class="action-btn delete-btn" onclick={onDelete} title="Delete">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M21 4H8l-7 8 7 8h13a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z"></path>
        <line x1="18" y1="9" x2="12" y2="15"></line>
        <line x1="12" y1="9" x2="18" y2="15"></line>
      </svg>
    </button>

    <button class="action-btn submit-btn" onclick={onSubmit} title="Submit">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <line x1="22" y1="2" x2="11" y2="13"></line>
        <polygon points="22 2 15 22 11 13 2 9 22 2"></polygon>
      </svg>
    </button>
  </div>
</div>

<style>
  .input-area {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    padding: 8px 16px;
  }

  .word-display {
    min-height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 2px;
    font-size: 28px;
    font-weight: 600;
    letter-spacing: 2px;
    color: var(--on-surface);
    border-bottom: 2px solid var(--outline);
    padding: 4px 16px;
    min-width: 200px;
    text-align: center;
    transition: border-color 0.2s;
  }
  .word-display.error { border-color: var(--error); }
  .word-display.success { border-color: var(--success); }
  .word-display.pangram { border-color: var(--tertiary); }

  .word-display span.center {
    color: var(--primary);
  }

  .placeholder {
    font-size: 16px;
    font-weight: 400;
    color: var(--on-surface-variant);
    letter-spacing: 0;
  }

  .message {
    font-size: 14px;
    font-weight: 500;
    letter-spacing: 0;
  }
  .error-msg { color: var(--error); }
  .success-msg { color: var(--success); }
  .pangram-msg { color: var(--tertiary); }

  .action-buttons {
    display: flex;
    gap: 16px;
    align-items: center;
  }

  .action-btn {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: var(--surface-variant);
    color: var(--on-surface-variant);
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.1s;
  }
  .action-btn:active { transform: scale(0.9); }

  .submit-btn {
    background: var(--primary);
    color: var(--on-primary);
    width: 56px;
    height: 56px;
  }
</style>
