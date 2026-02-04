<script lang="ts">
  import { settings, type ThemeMode } from '$lib/stores/settingsStore.svelte';

  interface Props {
    onBack: () => void;
  }

  let { onBack }: Props = $props();

  const dictLevels = [
    { value: 60, label: 'Easy (Level 60)', desc: '~75,000 common words' },
    { value: 70, label: 'Standard (Level 70)', desc: '~110,000 words' },
    { value: 80, label: 'Hard (Level 80)', desc: '~235,000 words' },
  ];

  const themes: { value: ThemeMode; label: string }[] = [
    { value: 'system', label: 'System' },
    { value: 'light', label: 'Light' },
    { value: 'dark', label: 'Dark' },
  ];
</script>

<div class="screen">
  <div class="top-bar">
    <button class="back-btn" onclick={onBack}>← Back</button>
    <h1>Settings</h1>
    <div style="width: 60px"></div>
  </div>

  <div class="content">
    <div class="settings-list">
      <div class="section">
        <h3>Dictionary</h3>
        {#each dictLevels as level}
          <label class="radio-item">
            <input
              type="radio"
              name="dict"
              checked={settings.value.dictionaryLevel === level.value}
              onchange={() => settings.setDictionaryLevel(level.value)}
            />
            <div>
              <div class="radio-label">{level.label}</div>
              <div class="radio-desc">{level.desc}</div>
            </div>
          </label>
        {/each}
      </div>

      <div class="section">
        <h3>Word Options</h3>
        <label class="toggle-item">
          <div>
            <div class="toggle-label">Include letter S</div>
            <div class="toggle-desc">Allow words with the letter S</div>
          </div>
          <input
            type="checkbox"
            class="toggle"
            checked={settings.value.includeS}
            onchange={(e) => settings.setIncludeS(e.currentTarget.checked)}
          />
        </label>
      </div>

      <div class="section">
        <h3>Hints</h3>
        <label class="toggle-item">
          <div>
            <div class="toggle-label">Unlock all hints</div>
            <div class="toggle-desc">Make all hint tiers available immediately</div>
          </div>
          <input
            type="checkbox"
            class="toggle"
            checked={settings.value.allHintsAvailable}
            onchange={(e) => settings.setAllHintsAvailable(e.currentTarget.checked)}
          />
        </label>
      </div>

      <div class="section">
        <h3>Theme</h3>
        {#each themes as theme}
          <label class="radio-item">
            <input
              type="radio"
              name="theme"
              checked={settings.value.themeMode === theme.value}
              onchange={() => settings.setThemeMode(theme.value)}
            />
            <div class="radio-label">{theme.label}</div>
          </label>
        {/each}
      </div>

      <div class="section about">
        <h3>About</h3>
        <p>Heptad v1.0.0</p>
        <p>A word puzzle game.</p>
      </div>
    </div>
  </div>
</div>

<style>
  .settings-list { padding: 16px; }
  .section { margin-bottom: 24px; }
  .section h3 { font-size: 13px; font-weight: 600; color: var(--primary); text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 8px; }

  .radio-item {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    padding: 10px 0;
    cursor: pointer;
  }
  .radio-item input[type="radio"] {
    margin-top: 3px;
    accent-color: var(--primary);
  }
  .radio-label { font-size: 15px; }
  .radio-desc { font-size: 13px; color: var(--on-surface-variant); }

  .toggle-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    cursor: pointer;
  }
  .toggle-label { font-size: 15px; }
  .toggle-desc { font-size: 13px; color: var(--on-surface-variant); }

  .toggle {
    width: 44px;
    height: 24px;
    accent-color: var(--primary);
  }

  .about p { font-size: 14px; color: var(--on-surface-variant); margin: 4px 0; }
</style>
