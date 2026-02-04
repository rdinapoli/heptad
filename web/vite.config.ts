import { defineConfig } from 'vite';
import { svelte } from '@sveltejs/vite-plugin-svelte';
import { VitePWA } from 'vite-plugin-pwa';

export default defineConfig({
  base: '/heptad/',
  plugins: [
    svelte(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'icons/*.png'],
      manifest: {
        name: 'Heptad',
        short_name: 'Heptad',
        description: 'A word puzzle game',
        theme_color: '#6750A4',
        background_color: '#1C1B1F',
        display: 'standalone',
        orientation: 'portrait',
        scope: '/heptad/',
        start_url: '/heptad/',
        icons: [
          {
            src: 'icons/icon-192.png',
            sizes: '192x192',
            type: 'image/png'
          },
          {
            src: 'icons/icon-512.png',
            sizes: '512x512',
            type: 'image/png'
          },
          {
            src: 'icons/icon-512.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable'
          }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        runtimeCaching: [
          {
            urlPattern: /\/data\/daily_puzzles\.json$/,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'daily-puzzles',
              expiration: { maxAgeSeconds: 86400 }
            }
          },
          {
            urlPattern: /\/data\/scowl_\d+\.txt$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'dictionaries',
              expiration: { maxAgeSeconds: 30 * 86400 }
            }
          },
          {
            urlPattern: /\/data\/definitions\/.+\.json\.gz$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'definitions',
              expiration: { maxAgeSeconds: 30 * 86400 }
            }
          },
          {
            urlPattern: /^https:\/\/api\.dictionaryapi\.dev\//,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'definition-api',
              expiration: { maxEntries: 200, maxAgeSeconds: 7 * 86400 }
            }
          }
        ]
      }
    })
  ],
  resolve: {
    alias: {
      '$lib': '/src/lib'
    }
  },
  worker: {
    format: 'es'
  }
});
