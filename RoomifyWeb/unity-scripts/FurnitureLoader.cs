using System.Collections;
using UnityEngine;
using UnityEngine.Networking;

/// <summary>
/// FurnitureLoader — downloads a GLB/glTF model at runtime from a URL.
///
/// IMPORTANT: This script requires a GLB runtime loader package.
/// Recommended options (choose one):
///
///   A) GLTFast (free, Unity official):
///      Window → Package Manager → search "glTFast" → Install
///      https://docs.unity3d.com/Packages/com.unity.cloud.gltfast@6.0/manual/index.html
///
///   B) Piglet (paid, ~$30 on Asset Store — easiest setup)
///
/// The stub below shows the GLTFast integration pattern.
/// </summary>
public class FurnitureLoader : MonoBehaviour
{
    [Tooltip("Parent transform for instantiated models")]
    [SerializeField] private Transform parent;

    [Tooltip("Loading indicator shown during download")]
    [SerializeField] private GameObject loadingIndicator;

    private GameObject _lastLoaded;

    // ── Public API ─────────────────────────────────────────────────────────────

    /// <summary>
    /// Downloads the model at url and instantiates it under parent.
    /// Call from ARManager after receiving the furniture payload.
    /// </summary>
    public IEnumerator LoadFromUrl(string url, System.Action<GameObject> onComplete = null)
    {
        Debug.Log($"[FurnitureLoader] Downloading: {url}");

        if (loadingIndicator != null) loadingIndicator.SetActive(true);
        if (_lastLoaded != null)      Destroy(_lastLoaded);

#if UNITY_WEBGL
        // WebGL doesn't support threading, so we use UnityWebRequest
        using var req = UnityWebRequest.Get(url);
        yield return req.SendWebRequest();

        if (loadingIndicator != null) loadingIndicator.SetActive(false);

        if (req.result != UnityWebRequest.Result.Success)
        {
            Debug.LogError($"[FurnitureLoader] Download failed: {req.error}");
            onComplete?.Invoke(null);
            yield break;
        }

        // ── GLTFast integration ────────────────────────────────────────────
        // Uncomment and use once GLTFast is installed:
        //
        // var gltf = new GLTFast.GltfImport();
        // bool success = await gltf.LoadGltfBinary(req.downloadHandler.data, new Uri(url));
        // if (success)
        // {
        //     await gltf.InstantiateMainSceneAsync(parent);
        //     _lastLoaded = parent.GetChild(parent.childCount - 1).gameObject;
        //     onComplete?.Invoke(_lastLoaded);
        // }
        // ──────────────────────────────────────────────────────────────────

        // Placeholder: purple cube until GLTFast is set up
        _lastLoaded = GameObject.CreatePrimitive(PrimitiveType.Cube);
        _lastLoaded.name = "FurniturePlaceholder";
        _lastLoaded.transform.SetParent(parent, false);
        var mat = new Material(Shader.Find("Standard")) { color = new Color(0.54f, 0.30f, 0.98f) };
        _lastLoaded.GetComponent<Renderer>().material = mat;

        Debug.Log("[FurnitureLoader] Placeholder cube spawned. Install GLTFast for real models.");
        onComplete?.Invoke(_lastLoaded);
#else
        // In the Unity Editor, just create a placeholder immediately
        if (loadingIndicator != null) loadingIndicator.SetActive(false);
        _lastLoaded = GameObject.CreatePrimitive(PrimitiveType.Cube);
        _lastLoaded.name = "FurniturePlaceholder_Editor";
        _lastLoaded.transform.SetParent(parent, false);
        onComplete?.Invoke(_lastLoaded);
        yield break;
#endif
    }

    public void ClearCurrent()
    {
        if (_lastLoaded != null) Destroy(_lastLoaded);
        _lastLoaded = null;
    }
}
