import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    // Entorno jsdom para simular el DOM del navegador
    environment: 'jsdom',
    // Soporte para ES Modules nativos
    globals: false,
    // Directorio raíz de los tests
    include: ['tests/**/*.test.js'],
    // Cobertura con v8
    coverage: {
      provider: 'v8',
      include: ['js/**/*.js'],
      exclude: []
    }
  }
})
