using System;
using System.Collections;
using System.Runtime.InteropServices;
using UnityEngine;
using UnityEngine.Networking;

#if UNITY_WEBGL
using WebXR;
#endif

/// <summary>
/// ARManager — the single entry point for all React ↔ Unity communication.
///
/// Attach this script to a GameObject named exactly "ARManager" in your Unity scene.
/// The name must match what React calls in:
///   sendMessage('ARManager', 'LoadFurnitureModel', payload)
/// </summary>
public class ARManager : MonoBehaviour
{
    // ── Inspector refs ─────────────────────────────────────────────────────────
    [Header("Scene References")]
    [Tooltip("Root transform where loaded furniture prefabs are placed")]
    [SerializeField] private Transform placementRoot;

    [Tooltip("Placeholder shown while the real model is downloading")]
    [SerializeField] private GameObject loadingPlaceholder;

    [Header("AR Settings")]
    [SerializeField] private float defaultScaleFactor = 1.0f;

    // ── JS bridge imports ──────────────────────────────────────────────────────
    // These are defined in Assets/Plugins/WebGL/RoomifyBridge.jslib
#if UNITY_WEBGL && !UNITY_EDITOR
    [DllImport("__Internal")] private static extern void SendPlacementToReact(string json);
    [DllImport("__Internal")] private static extern void SendStatusToReact(string status);
#endif

    // ── Internal state ─────────────────────────────────────────────────────────
    private FurniturePayload _currentPayload;
    private GameObject       _currentModel;
    private bool             _arSessionActive;

    // ── Unity lifecycle ────────────────────────────────────────────────────────
    private void Awake()
    {
        NotifyStatus("idle");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API — called by React via sendMessage()
    // ─────────────────────────────────────────────────────────────────────────

    /// <summary>
    /// Receives JSON payload from React (furniture item to preview).
    /// React call: sendMessage('ARManager', 'LoadFurnitureModel', jsonString)
    /// </summary>
    public void LoadFurnitureModel(string jsonData)
    {
        try
        {
            _currentPayload = JsonUtility.FromJson<FurniturePayload>(jsonData);
            Debug.Log($"[ARManager] Loading furniture: {_currentPayload.name} | URL: {_currentPayload.model_url}");
            NotifyStatus($"loading:{_currentPayload.name}");
            StartCoroutine(DownloadAndPlace(_currentPayload));
        }
        catch (Exception ex)
        {
            Debug.LogError($"[ARManager] LoadFurnitureModel parse error: {ex.Message}");
            NotifyStatus("error:invalid_payload");
        }
    }

    /// <summary>
    /// Starts the WebXR AR session (activates camera passthrough).
    /// React call: sendMessage('ARManager', 'StartARSession', '')
    /// </summary>
    public void StartARSession(string _)
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        WebXRManager.Instance.ToggleAR();
        _arSessionActive = true;
        NotifyStatus("ar_session_started");
#else
        Debug.Log("[ARManager] StartARSession — WebXR not available in editor, running in 3D preview mode.");
        NotifyStatus("preview_mode");
#endif
    }

    /// <summary>
    /// Resets the placed model and clears the scene.
    /// React call: sendMessage('ARManager', 'ResetPlacement', '')
    /// </summary>
    public void ResetPlacement(string _)
    {
        if (_currentModel != null)
        {
            Destroy(_currentModel);
            _currentModel = null;
        }
        NotifyStatus("reset");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal — model download & placement
    // ─────────────────────────────────────────────────────────────────────────

    private IEnumerator DownloadAndPlace(FurniturePayload payload)
    {
        // Show placeholder while downloading
        if (loadingPlaceholder != null) loadingPlaceholder.SetActive(true);
        if (_currentModel != null)      Destroy(_currentModel);

        // Download the GLB/glTF model
        using var req = UnityWebRequest.Get(payload.model_url);
        req.downloadHandler = new DownloadHandlerBuffer();
        yield return req.SendWebRequest();

        if (loadingPlaceholder != null) loadingPlaceholder.SetActive(false);

        if (req.result != UnityWebRequest.Result.Success)
        {
            Debug.LogError($"[ARManager] Failed to download model: {req.error}");
            NotifyStatus($"error:download_failed");
            yield break;
        }

        // NOTE: For production, use a GLB importer (e.g. GLTFast or Piglet).
        // Here we create a placeholder cube at the hit-test position as a stand-in.
        // Replace this block with your actual GLB loading code.
        _currentModel = GameObject.CreatePrimitive(PrimitiveType.Cube);
        _currentModel.name = payload.name;
        _currentModel.transform.SetParent(placementRoot, false);
        _currentModel.transform.localScale = Vector3.one * defaultScaleFactor;

        // Apply a tinted material so it's visible
        var mat = new Material(Shader.Find("Standard"));
        mat.color = new Color(0.54f, 0.30f, 0.98f, 1f); // Roomify purple
        _currentModel.GetComponent<Renderer>().material = mat;

        NotifyStatus($"model_ready:{payload.name}");
        Debug.Log($"[ARManager] Model ready: {payload.name}");
    }

    /// <summary>
    /// Call this from your hit-test / gesture code when the user confirms placement.
    /// </summary>
    public void ConfirmPlacement(Vector3 worldPosition)
    {
        if (_currentPayload == null || _currentModel == null) return;

        _currentModel.transform.position = worldPosition;

        var result = new PlacementResult
        {
            placed  = true,
            item_id = _currentPayload.id,
            x       = worldPosition.x,
            y       = worldPosition.y,
            z       = worldPosition.z,
        };

        string json = JsonUtility.ToJson(result);
        Debug.Log($"[ARManager] Placement confirmed: {json}");

#if UNITY_WEBGL && !UNITY_EDITOR
        SendPlacementToReact(json);
#else
        // In editor, simulate the callback so you can test without a browser
        Debug.Log($"[ARManager] Would fire SendPlacementToReact({json})");
#endif
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void NotifyStatus(string status)
    {
#if UNITY_WEBGL && !UNITY_EDITOR
        SendStatusToReact(status);
#else
        Debug.Log($"[ARManager] Status: {status}");
#endif
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Serialisable data types (must match React JSON payloads)
    // ─────────────────────────────────────────────────────────────────────────

    [Serializable]
    public class FurniturePayload
    {
        public string id;
        public string name;
        public string model_url;
        public string category;
        public float  price;
    }

    [Serializable]
    public class PlacementResult
    {
        public bool   placed;
        public string item_id;
        public float  x;
        public float  y;
        public float  z;
    }
}
