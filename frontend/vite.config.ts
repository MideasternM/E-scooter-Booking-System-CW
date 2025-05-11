import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
// vitest specific imports
import { defineConfig as defineVitestConfig } from 'vitest/config'
import { mergeConfig } from 'vite'

const vitestConfig = defineVitestConfig({
  test: {
    globals: true,
    environment: 'happy-dom', // or 'jsdom'
    setupFiles: [], // Optional: for global setup files
    coverage: { // Optional: for coverage reports
      provider: 'v8', // or 'istanbul'
      reporter: ['text', 'json', 'html'],
    },
  },
})

export default mergeConfig(defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false
      }
    }
  }
}), vitestConfig)
