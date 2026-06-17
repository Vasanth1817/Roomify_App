import React, { useEffect, useCallback, useState, useRef } from 'react';
import { Unity, useUnityContext } from 'react-unity-webgl';
import { useUnityAR } from '../context/UnityARContext';
import { X, Camera, CheckCircle, AlertTriangle, Maximize2, Minimize2 } from 'lucide-react';
import './UnityARViewer.css';

/**
 * UnityRealViewer
 * Renders the actual Unity WebGL canvas.
 * Only imported when /unity/RoomifyAR.loader.js exists (checked by UnityARViewer).
 *
 * Two-way communication:
 *   React → Unity : sendMessage('ARManager', 'LoadFurnitureModel', jsonPayload)
 *   Unity → React : addEventListener('OnFurniturePlaced', handler)
 *                   addEventListener('OnARStatus', handler)
 */
const UnityRealViewer = ({ onClose }) => {
  const { selectedFurniture, recordPlacement, setArStatus } = useUnityAR();
  const [loadPct, setLoadPct]           = useState(0);
  const [error, setError]               = useState(null);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [statusMsg, setStatusMsg]       = useState('');
  const sentRef = useRef(false);

  const {
    unityProvider,
    sendMessage,
    addEventListener,
    removeEventListener,
    isLoaded,
    loadingProgression,
    initialisationError,
  } = useUnityContext({
    loaderUrl:    '/unity/RoomifyAR.loader.js',
    dataUrl:      '/unity/RoomifyAR.data.br',
    frameworkUrl: '/unity/RoomifyAR.framework.js.br',
    codeUrl:      '/unity/RoomifyAR.wasm.br',
    webglContextAttributes: {
      powerPreference: 'high-performance',
      antialias: false,
      preserveDrawingBuffer: true,
    },
    matchWebGLToCanvasSize: true,
    devicePixelRatio: window.devicePixelRatio,
  });

  useEffect(() => { setLoadPct(Math.round(loadingProgression * 100)); }, [loadingProgression]);

  useEffect(() => {
    if (initialisationError) setError(`Unity failed to initialise: ${initialisationError.message}`);
  }, [initialisationError]);

  const handlePlacement = useCallback((jsonString) => {
    try {
      const data = JSON.parse(jsonString);
      recordPlacement({ ...data, item: selectedFurniture });
      setStatusMsg(`✓ ${selectedFurniture?.name ?? 'Item'} placed!`);
      setTimeout(() => setStatusMsg(''), 3000);
    } catch (e) {
      console.error('[UnityRealViewer] placement parse error:', e);
    }
  }, [recordPlacement, selectedFurniture]);

  const handleStatus = useCallback((status) => {
    setArStatus(status);
    setStatusMsg(status);
    setTimeout(() => setStatusMsg(''), 4000);
  }, [setArStatus]);

  useEffect(() => {
    addEventListener('OnFurniturePlaced', handlePlacement);
    addEventListener('OnARStatus', handleStatus);
    return () => {
      removeEventListener('OnFurniturePlaced', handlePlacement);
      removeEventListener('OnARStatus', handleStatus);
    };
  }, [addEventListener, removeEventListener, handlePlacement, handleStatus]);

  useEffect(() => {
    if (!isLoaded || !selectedFurniture || sentRef.current) return;
    sentRef.current = true;
    const payload = JSON.stringify({
      id:        selectedFurniture.id ?? selectedFurniture.furniture_id ?? '',
      name:      selectedFurniture.name ?? '',
      model_url: selectedFurniture.model_url ?? '',
      category:  selectedFurniture.category ?? '',
      price:     selectedFurniture.price ?? 0,
    });
    setTimeout(() => sendMessage('ARManager', 'LoadFurnitureModel', payload), 500);
  }, [isLoaded, selectedFurniture, sendMessage]);

  const startARSession = useCallback(() => {
    if (isLoaded) sendMessage('ARManager', 'StartARSession', '');
  }, [isLoaded, sendMessage]);

  const resetPlacement = useCallback(() => {
    if (isLoaded) { sentRef.current = false; sendMessage('ARManager', 'ResetPlacement', ''); }
  }, [isLoaded, sendMessage]);

  const toggleFullscreen = useCallback(() => {
    if (!document.fullscreenElement) {
      document.getElementById('unity-ar-root')?.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  }, []);

  useEffect(() => () => { sentRef.current = false; }, []);

  return (
    <div className="uar-overlay" id="unity-ar-root">
      <div className={`uar-container ${isFullscreen ? 'uar-fullscreen' : ''}`}>
        <div className="uar-header">
          <div className="uar-header-left">
            <span className="uar-dot" />
            <Camera size={16} />
            <span className="uar-title">AR Room Planner (Unity)</span>
            {selectedFurniture && (
              <span className="uar-item-badge">{selectedFurniture.name}</span>
            )}
          </div>
          <div className="uar-header-right">
            {isLoaded && (
              <>
                <button className="uar-btn-outline" onClick={resetPlacement}>Reset</button>
                <button className="uar-btn-primary" onClick={startARSession}>🥽 Start AR</button>
              </>
            )}
            <button className="uar-icon-btn" onClick={toggleFullscreen}>
              {isFullscreen ? <Minimize2 size={17} /> : <Maximize2 size={17} />}
            </button>
            <button className="uar-icon-btn uar-close-btn" onClick={onClose}><X size={17} /></button>
          </div>
        </div>

        {statusMsg && (
          <div className="uar-status-toast">
            <CheckCircle size={14} />
            <span>{statusMsg}</span>
          </div>
        )}

        {!isLoaded && !error && (
          <div className="uar-loading">
            <div className="uar-loading-card">
              <div className="uar-spinner-ring" />
              <h3 className="uar-loading-title">Loading Unity AR Engine</h3>
              <p className="uar-loading-sub">Initialising Unity WebGL + WebXR…</p>
              <div className="uar-progress-track">
                <div className="uar-progress-fill" style={{ width: `${loadPct}%` }} />
              </div>
              <span className="uar-progress-num">{loadPct}%</span>
              {loadPct < 20 && (
                <p className="uar-loading-hint">
                  ⏳ First load downloads the WebGL bundle (~100 MB). Subsequent loads use cache.
                </p>
              )}
            </div>
          </div>
        )}

        {error && (
          <div className="uar-loading">
            <div className="uar-loading-card uar-error-card">
              <AlertTriangle size={40} color="#ff6b6b" />
              <h3 style={{ color: '#ff6b6b', marginTop: '1rem' }}>Unity failed to load</h3>
              <p className="uar-loading-sub">{error}</p>
              <button className="uar-btn-primary" style={{ marginTop: '1.5rem' }} onClick={onClose}>Close</button>
            </div>
          </div>
        )}

        <Unity
          unityProvider={unityProvider}
          className="uar-canvas"
          style={{ opacity: isLoaded ? 1 : 0, pointerEvents: isLoaded ? 'auto' : 'none' }}
        />

        {isLoaded && (
          <div className="uar-footer">
            <span>Tap a surface to place furniture · Pinch to scale · Two-finger rotate</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default UnityRealViewer;
