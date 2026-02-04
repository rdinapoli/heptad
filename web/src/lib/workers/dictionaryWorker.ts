// Web Worker for parsing dictionary text into a Set of words
// Runs off the main thread to avoid blocking UI

self.onmessage = (e: MessageEvent<string>) => {
  const text = e.data;
  const words: string[] = [];
  const lines = text.split('\n');
  for (const line of lines) {
    const word = line.trim().toLowerCase();
    if (word.length >= 4 && /^[a-z]+$/.test(word)) {
      words.push(word);
    }
  }
  self.postMessage(words);
};
