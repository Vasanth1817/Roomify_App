# Unity WebGL Build Files

Place your exported Unity WebGL build files here after exporting from Unity Editor:

```
public/unity/
  RoomifyAR.loader.js          ← Unity loader script
  RoomifyAR.data.br            ← Compressed game data (may be 50–200 MB)
  RoomifyAR.framework.js.br    ← Unity framework
  RoomifyAR.wasm.br            ← WebAssembly binary
```

## Export Steps (in Unity Editor)
1. File → Build Settings → WebGL → Switch Platform
2. Player Settings → Memory Size: 512 MB
3. Player Settings → Compression: Brotli
4. WebGL Template: WebXR (from de-panther/unity-webxr-export)
5. File → Build Settings → Build
6. Copy the output Build/ folder contents into this directory

## Notes
- Files with .br extension are Brotli-compressed
- The server must send `Content-Encoding: br` for .br files
- Vite dev server handles this automatically via the vite.config.js assetsInclude rule
