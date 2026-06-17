import React, { useEffect, useCallback, useState, useRef } from 'react';
import SimulatedARViewer from './SimulatedARViewer';

/**
 * UnityARViewer — smart wrapper.
 *
 * 1. On mount, probes whether the Unity WebGL loader file exists
 *    at /unity/RoomifyAR.loader.js  (i.e. you've exported from Unity Editor).
 * 2. If found  → loads real Unity WebGL viewer (react-unity-webgl).
 * 3. If absent → renders SimulatedARViewer (camera + canvas AR demo).
 *
 * This means the app is ALWAYS runnable.  Once you export from Unity and
 * copy the Build/ folder into public/unity/, the real AR engine takes over
 * automatically with no code changes.
 */

/* ── lazy-load the real Unity viewer only when files are present ── */
const UnityRealViewer = React.lazy(() => import('./UnityRealViewer'));

const UnityARViewer = ({ onClose }) => {
  const [hasUnityFiles, setHasUnityFiles] = useState(null); // null = checking

  useEffect(() => {
    /* Probe for the Unity loader script — lightweight HEAD request */
    fetch('/unity/RoomifyAR.loader.js', { method: 'HEAD' })
      .then(res => setHasUnityFiles(res.ok))
      .catch(() => setHasUnityFiles(false));
  }, []);

  /* Still checking */
  if (hasUnityFiles === null) {
    return (
      <div style={{
        position: 'fixed', inset: 0, zIndex: 9999,
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        background: 'rgba(4,4,18,0.92)',
      }}>
        <div style={{ textAlign: 'center', color: '#8B4DFA' }}>
          <div style={{
            width: 40, height: 40,
            border: '3px solid rgba(139,77,250,0.2)',
            borderTopColor: '#8B4DFA',
            borderRadius: '50%',
            animation: 'spin 0.7s linear infinite',
            margin: '0 auto 0.75rem',
          }} />
          <p style={{ color: '#9988cc', fontSize: '0.85rem' }}>Initialising AR…</p>
          <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
        </div>
      </div>
    );
  }

  /* Unity WebGL build found → use real engine */
  if (hasUnityFiles) {
    return (
      <React.Suspense fallback={null}>
        <UnityRealViewer onClose={onClose} />
      </React.Suspense>
    );
  }

  /* No Unity build → camera-based AR demo */
  return <SimulatedARViewer onClose={onClose} />;
};

export default UnityARViewer;
