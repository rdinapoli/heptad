import { idbGet, idbSet } from './storageService';

export interface Definition {
  word: string;
  phonetic?: string;
  meanings: DefinitionMeaning[];
}

export interface DefinitionMeaning {
  partOfSpeech: string;
  definitions: { definition: string; example?: string }[];
}

export type DefinitionResult =
  | { type: 'loading' }
  | { type: 'success'; definition: Definition }
  | { type: 'notFound' }
  | { type: 'error'; message: string };

const BASE = import.meta.env.BASE_URL;
const letterCache = new Map<string, Map<string, Definition>>();

async function loadLetterChunk(letter: string): Promise<Map<string, Definition>> {
  if (letterCache.has(letter)) return letterCache.get(letter)!;

  // Check IndexedDB
  const cached = await idbGet<Record<string, Definition>>(`defs_${letter}`);
  if (cached) {
    const map = new Map(Object.entries(cached));
    letterCache.set(letter, map);
    return map;
  }

  // Try fetching the chunk
  try {
    const res = await fetch(`${BASE}data/definitions/${letter}.json`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const data: Record<string, Definition> = await res.json();
    const map = new Map(Object.entries(data));
    letterCache.set(letter, map);
    await idbSet(`defs_${letter}`, data);
    return map;
  } catch {
    const empty = new Map<string, Definition>();
    letterCache.set(letter, empty);
    return empty;
  }
}

async function fetchFromAPI(word: string): Promise<Definition | null> {
  try {
    const res = await fetch(`https://api.dictionaryapi.dev/api/v2/entries/en/${encodeURIComponent(word)}`);
    if (!res.ok) return null;
    const data = await res.json();
    if (!Array.isArray(data) || data.length === 0) return null;
    const entry = data[0];
    return {
      word: entry.word ?? word,
      phonetic: entry.phonetic,
      meanings: (entry.meanings ?? []).map((m: any) => ({
        partOfSpeech: m.partOfSpeech ?? '',
        definitions: (m.definitions ?? []).map((d: any) => ({
          definition: d.definition ?? '',
          example: d.example
        }))
      }))
    };
  } catch {
    return null;
  }
}

export async function getDefinition(word: string): Promise<DefinitionResult> {
  const w = word.toLowerCase();
  const letter = w[0];

  // Try local definitions first
  const chunk = await loadLetterChunk(letter);
  const local = chunk.get(w);
  if (local) return { type: 'success', definition: local };

  // API fallback
  const apiDef = await fetchFromAPI(w);
  if (apiDef) return { type: 'success', definition: apiDef };

  return { type: 'notFound' };
}
