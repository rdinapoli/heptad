<script lang="ts">
  interface Props {
    centerLetter: string;
    outerLetters: string[];
    onLetterClick: (letter: string) => void;
  }

  let { centerLetter, outerLetters, onLetterClick }: Props = $props();
  let pressedLetter = $state<string | null>(null);

  function handlePress(letter: string) {
    pressedLetter = letter;
    onLetterClick(letter);
    setTimeout(() => { pressedLetter = null; }, 150);
  }
</script>

<div class="orbital-container">
  <svg class="orbital-ring" viewBox="0 0 300 300">
    <circle cx="150" cy="150" r="100" fill="none" stroke="var(--outline)" stroke-width="1.5" opacity="0.25" />
  </svg>

  {#each outerLetters as letter, i}
    {@const angle = (i * 60 - 90) * Math.PI / 180}
    {@const x = 50 + 33 * Math.cos(angle)}
    {@const y = 50 + 33 * Math.sin(angle)}
    <button
      class="orbital-letter"
      class:pressed={pressedLetter === letter}
      style="left: {x}%; top: {y}%"
      onpointerdown={() => handlePress(letter)}
    >
      {letter.toUpperCase()}
    </button>
  {/each}

  <button
    class="center-letter"
    class:pressed={pressedLetter === centerLetter}
    onpointerdown={() => handlePress(centerLetter)}
  >
    {centerLetter.toUpperCase()}
  </button>
</div>

<style>
  .orbital-container {
    position: relative;
    width: min(300px, 80vw);
    height: min(300px, 80vw);
    margin: 0 auto;
  }

  .orbital-ring {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  .center-letter {
    position: absolute;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: var(--primary-container);
    border: 3px solid var(--primary);
    color: var(--on-primary-container);
    font-size: 28px;
    font-weight: 700;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    transition: transform 0.15s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .center-letter.pressed {
    transform: translate(-50%, -50%) scale(0.88);
  }

  .orbital-letter {
    position: absolute;
    transform: translate(-50%, -50%);
    width: 52px;
    height: 52px;
    border-radius: 50%;
    background: var(--secondary-container);
    border: 2px solid var(--secondary);
    color: var(--on-secondary-container);
    font-size: 22px;
    font-weight: 600;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    transition: transform 0.15s cubic-bezier(0.34, 1.56, 0.64, 1);
  }
  .orbital-letter.pressed {
    transform: translate(-50%, -50%) scale(0.82);
  }
</style>
