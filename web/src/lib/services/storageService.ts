// localStorage wrapper with heptad_ prefix

const PREFIX = 'heptad_';

export function getItem(key: string): string | null {
  return localStorage.getItem(PREFIX + key);
}

export function setItem(key: string, value: string): void {
  localStorage.setItem(PREFIX + key, value);
}

export function getInt(key: string, defaultValue: number = 0): number {
  const v = getItem(key);
  return v !== null ? parseInt(v, 10) : defaultValue;
}

export function setInt(key: string, value: number): void {
  setItem(key, String(value));
}

export function getBool(key: string, defaultValue: boolean = false): boolean {
  const v = getItem(key);
  return v !== null ? v === 'true' : defaultValue;
}

export function setBool(key: string, value: boolean): void {
  setItem(key, String(value));
}

export function getJSON<T>(key: string, defaultValue: T): T {
  const v = getItem(key);
  if (v === null) return defaultValue;
  try {
    return JSON.parse(v) as T;
  } catch {
    return defaultValue;
  }
}

export function setJSON(key: string, value: unknown): void {
  setItem(key, JSON.stringify(value));
}

// IndexedDB for larger data (dictionaries, definitions)
const DB_NAME = 'heptad';
const DB_VERSION = 1;

function openDB(): Promise<IDBDatabase> {
  return new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = () => {
      const db = req.result;
      if (!db.objectStoreNames.contains('cache')) {
        db.createObjectStore('cache');
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

export async function idbGet<T>(key: string): Promise<T | undefined> {
  const db = await openDB();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('cache', 'readonly');
    const store = tx.objectStore('cache');
    const req = store.get(key);
    req.onsuccess = () => resolve(req.result as T | undefined);
    req.onerror = () => reject(req.error);
  });
}

export async function idbSet(key: string, value: unknown): Promise<void> {
  const db = await openDB();
  return new Promise((resolve, reject) => {
    const tx = db.transaction('cache', 'readwrite');
    const store = tx.objectStore('cache');
    const req = store.put(value, key);
    req.onsuccess = () => resolve();
    req.onerror = () => reject(req.error);
  });
}
