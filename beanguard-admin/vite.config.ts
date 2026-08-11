import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'prompt',
      injectRegister: false,
      strategies: 'generateSW',
      manifestFilename: 'manifest.json',
      includeAssets: [
        'favicon.ico',
        'robots.txt',
        'logo.png',
        'logo-192.png',
        'logo-maskable.png',
      ],
      manifest: {
        id: '/',
        scope: '/',
        start_url: '.',
        name: 'BeanGuard - Panel administracyjny',
        short_name: 'BeanGuard',
        display: 'standalone',
        theme_color: '#18181b',
        background_color: '#ffffff',
        icons: [
          {
            src: 'logo-192.png',
            sizes: '192x192',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: 'logo.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'any',
          },
          {
            src: 'logo-maskable.png',
            sizes: '512x512',
            type: 'image/png',
            purpose: 'maskable',
          },
        ],
      },
      workbox: {
        navigateFallbackDenylist: [/^\/api\//],
      },
    }),
  ],
  build: {
    outDir: 'build',
  },
})
