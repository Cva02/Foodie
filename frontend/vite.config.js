import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // React calls /api/... and Vite forwards it to the Spring Boot server
    proxy: { '/api': 'http://localhost:8080' },
  },
});
