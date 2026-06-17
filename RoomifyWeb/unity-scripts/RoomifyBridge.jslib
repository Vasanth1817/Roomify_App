/**
 * RoomifyBridge.jslib
 *
 * Unity WebGL JavaScript plugin that bridges C# DllImport calls to
 * react-unity-webgl's dispatchReactUnityEvent() function.
 *
 * INSTALL: Copy this file to
 *   Assets/Plugins/WebGL/RoomifyBridge.jslib
 * inside your Unity project. Unity will automatically include it
 * in the WebGL build.
 *
 * HOW IT WORKS:
 *   1. C# calls  SendPlacementToReact(json)  via [DllImport("__Internal")]
 *   2. Unity's IL2CPP backend looks up this jslib and calls the JS below
 *   3. dispatchReactUnityEvent() is injected by react-unity-webgl v9+
 *      and triggers the addEventListener('OnFurniturePlaced', ...) handler
 *      registered in UnityARViewer.jsx
 */

mergeInto(LibraryManager.library, {

  /**
   * Fires the "OnFurniturePlaced" React event with a JSON payload.
   * C# signature: extern void SendPlacementToReact(string json)
   *
   * Payload shape (matches PlacementResult in ARManager.cs):
   *   { placed: true, item_id: "...", x: 1.0, y: 0.0, z: -2.5 }
   */
  SendPlacementToReact: function (jsonPtr) {
    var json = UTF8ToString(jsonPtr);
    if (typeof dispatchReactUnityEvent === "function") {
      dispatchReactUnityEvent("OnFurniturePlaced", json);
    } else {
      console.warn("[RoomifyBridge] dispatchReactUnityEvent not found — " +
        "ensure react-unity-webgl v9+ is installed.");
    }
  },

  /**
   * Fires the "OnARStatus" React event with a plain status string.
   * C# signature: extern void SendStatusToReact(string status)
   *
   * Status strings:
   *   "idle"                  — initial state
   *   "loading:<name>"        — downloading model
   *   "model_ready:<name>"    — model placed in scene
   *   "ar_session_started"    — WebXR camera active
   *   "preview_mode"          — 3D only (no WebXR)
   *   "reset"                 — scene cleared
   *   "error:<reason>"        — something went wrong
   */
  SendStatusToReact: function (statusPtr) {
    var status = UTF8ToString(statusPtr);
    if (typeof dispatchReactUnityEvent === "function") {
      dispatchReactUnityEvent("OnARStatus", status);
    }
  },

});
