import { getDefinition, type DefinitionResult } from '../services/definitionService';

let _result = $state<DefinitionResult>({ type: 'loading' });
let _word = $state<string | null>(null);

export const definitionStore = {
  get result() { return _result; },
  get word() { return _word; },

  async fetch(word: string) {
    _word = word;
    _result = { type: 'loading' };
    _result = await getDefinition(word);
  },

  clear() {
    _word = null;
    _result = { type: 'loading' };
  }
};
