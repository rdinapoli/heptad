<script lang="ts">
  import MainMenu from './routes/MainMenu.svelte';
  import Game from './routes/Game.svelte';
  import DailyPuzzle from './routes/DailyPuzzle.svelte';
  import Settings from './routes/Settings.svelte';
  import { applyTheme } from '$lib/stores/settingsStore.svelte';
  import { settings } from '$lib/stores/settingsStore.svelte';
  import { onMount } from 'svelte';

  let route = $state('menu');

  onMount(() => {
    applyTheme(settings.value.themeMode);

    // Handle browser back
    const handlePop = () => {
      const hash = location.hash.slice(1) || 'menu';
      route = hash;
    };
    window.addEventListener('popstate', handlePop);
    handlePop();
    return () => window.removeEventListener('popstate', handlePop);
  });

  function navigate(r: string) {
    route = r;
    history.pushState(null, '', `#${r}`);
  }

  function goBack() {
    route = 'menu';
    history.pushState(null, '', '#menu');
  }
</script>

{#if route === 'menu'}
  <MainMenu onNavigate={navigate} />
{:else if route === 'game'}
  <Game onBack={goBack} />
{:else if route === 'daily'}
  <DailyPuzzle onBack={goBack} />
{:else if route === 'settings'}
  <Settings onBack={goBack} />
{:else}
  <MainMenu onNavigate={navigate} />
{/if}
