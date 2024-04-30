import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import fs from 'fs'

// https://vitejs.dev/config/
export default defineConfig({
  //  base: '/tb-ui/',
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  server: {
    // https://stackoverflow.com/a/69743888
    https: {
      key: fs.readFileSync('/Users/gmazza/opensource/certs/tightblog.key'),
      cert: fs.readFileSync('/Users/gmazza/opensource/certs/tightblog.crt')
    },
    host: 'localhost',
    // https://stackoverflow.com/q/62944640/1207540
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers':
        'Cache-Control, Pragma, Origin, Authorization, Content-Type, X-Requested-With',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE'
    },
    proxy: {
      '/tb-ui': {
        target: 'https://localhost:8443/',
        secure: false
      }
    }
  }
})
