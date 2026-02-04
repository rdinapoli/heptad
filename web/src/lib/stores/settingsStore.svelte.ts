import * as storage from '../services/storageService';

export type ThemeMode = 'system' | 'light' | 'dark';

export interface Settings {
  dictionaryLevel: number;
  includeS: boolean;
  themeMode: ThemeMode;
  allHintsAvailable: boolean;
}

function loadSettings(): Settings {
  return {
    dictionaryLevel: storage.getInt('dict_level', 70),
    includeS: storage.getBool('include_s', true),
    themeMode: (storage.getItem('theme') as ThemeMode) ?? 'system',
    allHintsAvailable: storage.getBool('all_hints', false)
  };
}

function saveSettings(s: Settings): void {
  storage.setInt('dict_level', s.dictionaryLevel);
  storage.setBool('include_s', s.includeS);
  storage.setItem('theme', s.themeMode);
  storage.setBool('all_hints', s.allHintsAvailable);
}

let _settings = $state<Settings>(loadSettings());

export const settings = {
  get value() { return _settings; },
  setDictionaryLevel(level: number) {
    _settings = { ..._settings, dictionaryLevel: level };
    saveSettings(_settings);
  },
  setIncludeS(v: boolean) {
    _settings = { ..._settings, includeS: v };
    saveSettings(_settings);
  },
  setThemeMode(mode: ThemeMode) {
    _settings = { ..._settings, themeMode: mode };
    saveSettings(_settings);
    applyTheme(mode);
  },
  setAllHintsAvailable(v: boolean) {
    _settings = { ..._settings, allHintsAvailable: v };
    saveSettings(_settings);
  }
};

export function applyTheme(mode: ThemeMode): void {
  const html = document.documentElement;
  if (mode === 'system') {
    html.removeAttribute('data-theme');
  } else {
    html.setAttribute('data-theme', mode);
  }
}
