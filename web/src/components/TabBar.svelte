<script lang="ts">
  interface Props {
    tabs: string[];
    activeTab: number;
    onTabChange: (index: number) => void;
  }

  let { tabs, activeTab, onTabChange }: Props = $props();
</script>

<div class="tab-bar">
  {#each tabs as tab, i}
    <button
      class="tab"
      class:active={activeTab === i}
      onclick={() => onTabChange(i)}
    >
      {tab}
    </button>
  {/each}
</div>

<style>
  .tab-bar {
    display: flex;
    border-bottom: 1px solid var(--outline);
    background: var(--surface);
  }

  .tab {
    flex: 1;
    padding: 12px;
    font-size: 14px;
    font-weight: 500;
    color: var(--on-surface-variant);
    background: transparent;
    border-bottom: 2px solid transparent;
    transition: color 0.2s, border-color 0.2s;
  }
  .tab.active {
    color: var(--primary);
    border-bottom-color: var(--primary);
  }
</style>
