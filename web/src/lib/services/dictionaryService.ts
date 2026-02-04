import { idbGet, idbSet } from './storageService';

const BASE = import.meta.env.BASE_URL;
const cache = new Map<number, Set<string>>();

function parseWordsInWorker(text: string): Promise<string[]> {
  return new Promise((resolve) => {
    const worker = new Worker(
      new URL('../workers/dictionaryWorker.ts', import.meta.url),
      { type: 'module' }
    );
    worker.onmessage = (e: MessageEvent<string[]>) => {
      resolve(e.data);
      worker.terminate();
    };
    worker.postMessage(text);
  });
}

export async function loadDictionary(level: number): Promise<Set<string>> {
  // Check memory cache
  if (cache.has(level)) return cache.get(level)!;

  // Check IndexedDB cache
  const cached = await idbGet<string[]>(`dict_${level}`);
  if (cached) {
    const set = new Set(cached);
    cache.set(level, set);
    return set;
  }

  // Fetch and parse
  const res = await fetch(`${BASE}data/scowl_${level}.txt`);
  const text = await res.text();
  const words = await parseWordsInWorker(text);
  const set = new Set(words);

  // Cache in IndexedDB
  await idbSet(`dict_${level}`, words);
  cache.set(level, set);
  return set;
}

export function getCachedDictionary(level: number): Set<string> | undefined {
  return cache.get(level);
}
