import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: "/Roomify_App/",

  server: {
    // Required for Unity WebGL threading + WebXR SharedArrayBuffer support
    headers: {
      'Cross-Origin-Opener-Policy':   'same-origin',
      'Cross-Origin-Embedder-Policy': 'require-corp',
    },
  },

  preview: {
    headers: {
      'Cross-Origin-Opener-Policy':   'same-origin',
      'Cross-Origin-Embedder-Policy': 'require-corp',
    },
  },

  build: {
    // Unity WebGL bundles are large — suppress the default 500 kB warning
    chunkSizeWarningLimit: 12000,
    rollupOptions: {
      output: {
        // Do not attempt to bundle Unity's own loader/framework
        manualChunks: undefined,
      },
    },
  },

  // Tell Vite not to try to process Unity's binary asset files
  assetsInclude: ['**/*.data', '**/*.wasm', '**/*.br', '**/*.unityweb'],

  optimizeDeps: {
    // react-unity-webgl uses dynamic requires that confuse esbuild pre-bundling
    exclude: ['react-unity-webgl'],
  },
})
