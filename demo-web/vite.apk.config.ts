// APK용 빌드: 모든 JS·CSS·글꼴을 index.html 한 파일에 넣는다 (file:// 에서도 동작하도록)
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { viteSingleFile } from 'vite-plugin-singlefile'

export default defineConfig({
  plugins: [react(), viteSingleFile()],
  build: { outDir: '../apk-app/app/src/main/assets', emptyOutDir: true, assetsInlineLimit: 100_000_000 },
})
