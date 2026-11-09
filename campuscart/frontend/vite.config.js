import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// In development, /api and /uploads are forwarded to the Spring Boot server.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
      '/uploads': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
