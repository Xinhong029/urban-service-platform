import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
// This configuration sets up Vite to work with React 
// and proxies API requests to the backend server during development.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080', 
    },
  },
})

// react里请求api，vite会转发到http://localhost:8080/api/
// 例如fetch('/api/analytics/top-service-types')会被转发到http://localhost:8080/api/analytics/top-service-types
// 这样就可以在开发环境中直接请求后端API，而不需要担心跨域问题。