import React, {
  useRef, useEffect, useState, useCallback, useMemo,
} from 'react';
import { useUnityAR } from '../context/UnityARContext';
import {
  X, Camera, CheckCircle, Trash2, RotateCcw, ZoomIn, ZoomOut,
  AlertTriangle, Maximize2, Minimize2, Move,
} from 'lucide-react';
import './SimulatedARViewer.css';

/* ─── constants ───────────────────────────────────────────────── */
const FURNITURE_COLORS = [
  '#8B4DFA', '#6C2FD9', '#A855F7', '#7C3AED', '#9333EA',
];

/* ─── tiny helpers ────────────────────────────────────────────── */
const lerp = (a, b, t) => a + (b - a) * t;

/**
 * SimulatedARViewer
 * A fully functional AR demo that runs in any browser.
 * Uses device camera (getUserMedia) as the live background, then
 * overlays draggable, scalable furniture items on a canvas.
 *
 * Fires the same UnityARContext callbacks as the real Unity viewer,
 * so ARPage placement history works identically.
 */
const SimulatedARViewer = ({ onClose }) => {
  /* context */
  const { selectedFurniture, recordPlacement } = useUnityAR();

  /* refs */
  const videoRef    = useRef(null);
  const canvasRef   = useRef(null);
  const streamRef   = useRef(null);
  const animRef     = useRef(null);
  const stateRef    = useRef({ items: [], dragging: null, mode: 'place' });

  /* ui state */
  const [camError, setCamError]     = useState(null);
  const [camReady, setCamReady]     = useState(false);
  const [placedItems, setPlacedItems] = useState([]); // mirror of stateRef.items
  const [mode, setMode]             = useState('place'); // 'place' | 'move'
  const [scale, setScale]           = useState(1.0);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [flash, setFlash]           = useState(null); // brief toast msg
  const [cameraFacing, setCameraFacing] = useState('environment'); // 'environment' | 'user'

  /* ── pick a stable color for this furniture item ── */
  const itemColor = useMemo(() => {
    if (!selectedFurniture) return FURNITURE_COLORS[0];
    const idx = (selectedFurniture.name?.charCodeAt(0) ?? 0) % FURNITURE_COLORS.length;
    return FURNITURE_COLORS[idx];
  }, [selectedFurniture]);

  /* ── start camera ─────────────────────────────────────────────── */
  const startCamera = useCallback(async (facing = 'environment') => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop());
      streamRef.current = null;
    }
    setCamReady(false);
    setCamError(null);

    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: {
          facingMode: { ideal: facing },
          width:  { ideal: 1280 },
          height: { ideal: 720 },
        },
        audio: false,
      });
      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
        setCamReady(true);
        setCameraFacing(facing);
      }
    } catch (err) {
      console.warn('[SimulatedAR] Camera error:', err);
      setCamError(
        err.name === 'NotAllowedError'
          ? 'Camera permission denied. Please allow camera access and retry.'
          : `Camera error: ${err.message}`,
      );
      setCamReady(true); // still show canvas in fallback mode
    }
  }, []);

  useEffect(() => {
    startCamera(cameraFacing);
    return () => {
      if (streamRef.current) streamRef.current.getTracks().forEach(t => t.stop());
      if (animRef.current) cancelAnimationFrame(animRef.current);
    };
  }, []); // eslint-disable-line

  /* ── render loop ─────────────────────────────────────────────── */
  const drawFrame = useCallback(() => {
    const canvas = canvasRef.current;
    const video  = videoRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    const W = canvas.width;
    const H = canvas.height;

    /* 1. draw camera feed or dark bg */
    if (video && video.readyState >= 2 && !camError) {
      ctx.drawImage(video, 0, 0, W, H);
      /* slight dark overlay so furniture pops */
      ctx.fillStyle = 'rgba(0,0,0,0.18)';
      ctx.fillRect(0, 0, W, H);
    } else {
      /* fallback gradient */
      const grad = ctx.createLinearGradient(0, 0, W, H);
      grad.addColorStop(0, '#0d0d1e');
      grad.addColorStop(1, '#1a0d2e');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, W, H);

      /* grid pattern */
      ctx.strokeStyle = 'rgba(139,77,250,0.08)';
      ctx.lineWidth = 1;
      for (let x = 0; x < W; x += 40) {
        ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, H); ctx.stroke();
      }
      for (let y = 0; y < H; y += 40) {
        ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(W, y); ctx.stroke();
      }
    }

    /* 2. AR reticle / crosshair in place mode */
    if (stateRef.current.mode === 'place') {
      const cx = W / 2;
      const cy = H / 2;
      const t  = Date.now() / 1000;
      const pulse = 1 + Math.sin(t * 3) * 0.06;

      ctx.save();
      ctx.translate(cx, cy);
      ctx.scale(pulse, pulse);

      /* outer ring */
      ctx.beginPath();
      ctx.arc(0, 0, 28, 0, Math.PI * 2);
      ctx.strokeStyle = 'rgba(139,77,250,0.5)';
      ctx.lineWidth = 2;
      ctx.stroke();

      /* inner dot */
      ctx.beginPath();
      ctx.arc(0, 0, 5, 0, Math.PI * 2);
      ctx.fillStyle = '#8B4DFA';
      ctx.fill();

      /* crosshair lines */
      ctx.strokeStyle = 'rgba(139,77,250,0.7)';
      ctx.lineWidth = 1.5;
      [-18, 18].forEach(dx => {
        ctx.beginPath(); ctx.moveTo(dx < 0 ? -36 : 14, 0); ctx.lineTo(dx, 0); ctx.stroke();
      });
      [-18, 18].forEach(dy => {
        ctx.beginPath(); ctx.moveTo(0, dy < 0 ? -36 : 14); ctx.lineTo(0, dy); ctx.stroke();
      });
      ctx.restore();
    }

    /* 3. draw placed items */
    const items = stateRef.current.items;
    items.forEach((item, idx) => {
      /* animate: grow in on first appearance */
      if (item.birthTime) {
        const age = (Date.now() - item.birthTime) / 1000;
        item.displayScale = Math.min(1, item.displayScale + 0.08);
        if (age > 5) item.birthTime = null; // stop animation
      }

      const sc  = item.scale * (item.displayScale ?? 1) * scale;
      const hw  = (item.w * sc) / 2;
      const hh  = (item.h * sc) / 2;
      const x   = item.x;
      const y   = item.y;
      const sel = stateRef.current.dragging === idx;

      ctx.save();
      ctx.translate(x, y);

      /* shadow */
      const shadowW = hw * 1.6;
      const shadowH = shadowW * 0.22;
      const shadowGrad = ctx.createRadialGradient(0, hh + 6, 0, 0, hh + 6, shadowW);
      shadowGrad.addColorStop(0, 'rgba(0,0,0,0.45)');
      shadowGrad.addColorStop(1, 'rgba(0,0,0,0)');
      ctx.beginPath();
      ctx.ellipse(0, hh + 6, shadowW, shadowH, 0, 0, Math.PI * 2);
      ctx.fillStyle = shadowGrad;
      ctx.fill();

      /* body rect with rounded corners */
      const bx = -hw, by = -hh;
      const r  = 10;
      ctx.beginPath();
      ctx.roundRect(bx, by, hw * 2, hh * 2, r);

      if (item.imgEl && item.imgEl.complete) {
        /* clip to rounded rect, draw thumbnail */
        ctx.save();
        ctx.clip();
        ctx.drawImage(item.imgEl, bx, by, hw * 2, hh * 2);
        ctx.restore();
        /* semi-transparent colour tint */
        ctx.fillStyle = `${item.color}22`;
        ctx.fill();
      } else {
        /* colour block fallback */
        ctx.fillStyle = item.color;
        ctx.fill();

        /* label */
        ctx.fillStyle = '#fff';
        ctx.font = `bold ${Math.max(10, 13 * sc)}px Inter, sans-serif`;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        const label = item.name.length > 12 ? item.name.slice(0, 11) + '…' : item.name;
        ctx.fillText(label, 0, 0);
      }

      /* border */
      ctx.strokeStyle = sel ? '#fff' : `${item.color}99`;
      ctx.lineWidth = sel ? 2.5 : 1.5;
      ctx.beginPath();
      ctx.roundRect(bx, by, hw * 2, hh * 2, r);
      ctx.stroke();

      /* item number badge */
      ctx.beginPath();
      ctx.arc(hw - 10, -hh + 10, 10, 0, Math.PI * 2);
      ctx.fillStyle = '#8B4DFA';
      ctx.fill();
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 10px Inter, sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(idx + 1, hw - 10, -hh + 10);

      ctx.restore();
    });

    /* 4. HUD overlays */
    /* bottom-left: placed count */
    if (items.length > 0) {
      ctx.fillStyle = 'rgba(0,0,0,0.55)';
      ctx.beginPath();
      ctx.roundRect(12, H - 44, 130, 32, 8);
      ctx.fill();
      ctx.fillStyle = '#8B4DFA';
      ctx.font = 'bold 12px Inter, sans-serif';
      ctx.textAlign = 'left';
      ctx.textBaseline = 'middle';
      ctx.fillText(`✓ ${items.length} item${items.length > 1 ? 's' : ''} placed`, 20, H - 28);
    }

    animRef.current = requestAnimationFrame(drawFrame);
  }, [camError, scale]);

  /* start render loop when camera is ready */
  useEffect(() => {
    if (!camReady) return;
    if (animRef.current) cancelAnimationFrame(animRef.current);
    animRef.current = requestAnimationFrame(drawFrame);
    return () => { if (animRef.current) cancelAnimationFrame(animRef.current); };
  }, [camReady, drawFrame]);

  /* ── resize canvas to match container ───────────────────────── */
  useEffect(() => {
    const resize = () => {
      const canvas = canvasRef.current;
      if (!canvas) return;
      const parent = canvas.parentElement;
      canvas.width  = parent.clientWidth;
      canvas.height = parent.clientHeight;
    };
    resize();
    const ro = new ResizeObserver(resize);
    if (canvasRef.current?.parentElement) ro.observe(canvasRef.current.parentElement);
    return () => ro.disconnect();
  }, []);

  /* ── pointer handlers ────────────────────────────────────────── */
  const getPos = (e) => {
    const rect = canvasRef.current.getBoundingClientRect();
    const cl   = e.touches ? e.touches[0] : e;
    return {
      x: (cl.clientX - rect.left) * (canvasRef.current.width  / rect.width),
      y: (cl.clientY - rect.top)  * (canvasRef.current.height / rect.height),
    };
  };

  const handlePointerDown = useCallback((e) => {
    const pos = getPos(e);
    const items = stateRef.current.items;

    if (stateRef.current.mode === 'move') {
      /* pick closest item */
      let closest = -1, closestDist = 50;
      items.forEach((item, i) => {
        const dx = pos.x - item.x;
        const dy = pos.y - item.y;
        const d  = Math.sqrt(dx * dx + dy * dy);
        const hw = (item.w * item.scale * scale) / 2;
        if (d < hw + 20 && d < closestDist) { closest = i; closestDist = d; }
      });
      stateRef.current.dragging = closest >= 0 ? closest : null;
      return;
    }

    /* place mode: add new item */
    if (!selectedFurniture) return;

    const img = new Image();
    img.crossOrigin = 'anonymous';
    img.src = selectedFurniture.thumbnail_url || '';
    img.onerror = () => {}; // fallback handled in draw

    const newItem = {
      x:    pos.x,
      y:    pos.y,
      w:    120,
      h:    80,
      scale: 1,
      displayScale: 0.1,
      birthTime: Date.now(),
      color: itemColor,
      name:  selectedFurniture.name,
      item:  selectedFurniture,
      imgEl: img,
      id:    Date.now(),
    };

    stateRef.current.items = [...items, newItem];
    setPlacedItems(prev => [...prev, newItem]);

    /* fire the same callback Unity would fire */
    recordPlacement({
      placed:  true,
      item_id: selectedFurniture.id ?? selectedFurniture.furniture_id ?? '',
      x: parseFloat((pos.x / canvasRef.current.width  * 10 - 5).toFixed(2)),
      y: 0,
      z: parseFloat((pos.y / canvasRef.current.height * 10 - 5).toFixed(2)),
      item: selectedFurniture,
    });

    setFlash(`✓ ${selectedFurniture.name} placed!`);
    setTimeout(() => setFlash(null), 2500);
  }, [selectedFurniture, itemColor, recordPlacement, scale]);

  const handlePointerMove = useCallback((e) => {
    if (stateRef.current.mode !== 'move') return;
    if (stateRef.current.dragging === null) return;
    const pos = getPos(e);
    const idx = stateRef.current.dragging;
    stateRef.current.items = stateRef.current.items.map((item, i) =>
      i === idx ? { ...item, x: pos.x, y: pos.y } : item,
    );
  }, []);

  const handlePointerUp = useCallback(() => {
    stateRef.current.dragging = null;
  }, []);

  /* ── controls ─────────────────────────────────────────────────── */
  const switchMode = (m) => {
    setMode(m);
    stateRef.current.mode = m;
  };

  const clearAll = () => {
    stateRef.current.items = [];
    setPlacedItems([]);
  };

  const undoLast = () => {
    stateRef.current.items = stateRef.current.items.slice(0, -1);
    setPlacedItems(prev => prev.slice(0, -1));
  };

  const flipCamera = () => {
    const next = cameraFacing === 'environment' ? 'user' : 'environment';
    startCamera(next);
  };

  const toggleFullscreen = () => {
    const el = document.getElementById('sim-ar-root');
    if (!document.fullscreenElement) {
      el?.requestFullscreen();
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  };

  /* ─────────────────────────────────────────────────────────────── */
  return (
    <div className="sim-overlay" id="sim-ar-root">
      <div className={`sim-container ${isFullscreen ? 'sim-fullscreen' : ''}`}>

        {/* ── header ── */}
        <div className="sim-header">
          <div className="sim-header-left">
            <span className="sim-live-dot" />
            <Camera size={15} />
            <span className="sim-title">AR Room Planner</span>
            {selectedFurniture && (
              <span className="sim-item-badge">{selectedFurniture.name}</span>
            )}
            <span className="sim-demo-badge">DEMO MODE</span>
          </div>
          <div className="sim-header-right">
            <button
              className={`sim-mode-btn ${mode === 'place' ? 'active' : ''}`}
              onClick={() => switchMode('place')}
              title="Tap to place furniture"
            >
              + Place
            </button>
            <button
              className={`sim-mode-btn ${mode === 'move' ? 'active' : ''}`}
              onClick={() => switchMode('move')}
              title="Drag to reposition"
            >
              <Move size={13} style={{ marginRight: 4 }} />
              Move
            </button>
            <button className="sim-icon-btn" onClick={() => setScale(s => Math.min(2, +(s + 0.1).toFixed(1)))} title="Scale up">
              <ZoomIn size={15} />
            </button>
            <button className="sim-icon-btn" onClick={() => setScale(s => Math.max(0.3, +(s - 0.1).toFixed(1)))} title="Scale down">
              <ZoomOut size={15} />
            </button>
            <button className="sim-icon-btn" onClick={undoLast} title="Undo last placement">
              <RotateCcw size={15} />
            </button>
            <button className="sim-icon-btn" onClick={clearAll} title="Clear all">
              <Trash2 size={15} />
            </button>
            <button className="sim-icon-btn" onClick={flipCamera} title="Flip camera">
              🔄
            </button>
            <button className="sim-icon-btn" onClick={toggleFullscreen} title="Fullscreen">
              {isFullscreen ? <Minimize2 size={15} /> : <Maximize2 size={15} />}
            </button>
            <button className="sim-icon-btn sim-close" onClick={onClose} title="Close">
              <X size={15} />
            </button>
          </div>
        </div>

        {/* ── camera error banner ── */}
        {camError && (
          <div className="sim-cam-error">
            <AlertTriangle size={15} />
            <span>{camError}</span>
            <span className="sim-error-hint">Showing simulation canvas instead.</span>
          </div>
        )}

        {/* ── flash toast ── */}
        {flash && (
          <div className="sim-toast">
            <CheckCircle size={14} />
            <span>{flash}</span>
          </div>
        )}

        {/* ── canvas area ── */}
        <div className="sim-canvas-wrap">
          {/* hidden video for camera feed */}
          <video
            ref={videoRef}
            className="sim-video-hidden"
            autoPlay
            playsInline
            muted
          />

          {/* AR canvas */}
          {camReady ? (
            <canvas
              ref={canvasRef}
              className="sim-canvas"
              onMouseDown={handlePointerDown}
              onMouseMove={handlePointerMove}
              onMouseUp={handlePointerUp}
              onTouchStart={handlePointerDown}
              onTouchMove={handlePointerMove}
              onTouchEnd={handlePointerUp}
              style={{ cursor: mode === 'move' ? 'grab' : 'crosshair' }}
            />
          ) : (
            <div className="sim-loading">
              <div className="sim-spinner" />
              <p>Starting camera…</p>
            </div>
          )}

          {/* place-mode hint */}
          {camReady && mode === 'place' && !selectedFurniture && (
            <div className="sim-hint">
              Go to Catalog → click 🥽 on any item, then tap here to place it
            </div>
          )}
          {camReady && mode === 'place' && selectedFurniture && (
            <div className="sim-hint">
              Tap anywhere on the camera view to place <strong>{selectedFurniture.name}</strong>
            </div>
          )}
          {camReady && mode === 'move' && (
            <div className="sim-hint">
              Drag any placed item to reposition it
            </div>
          )}
        </div>

        {/* ── footer: scale indicator ── */}
        <div className="sim-footer">
          <span>Scale: {scale.toFixed(1)}×</span>
          <span>·</span>
          <span>{placedItems.length} item{placedItems.length !== 1 ? 's' : ''} placed</span>
          {placedItems.length > 0 && (
            <>
              <span>·</span>
              <span style={{ color: '#8B4DFA' }}>
                ₹{placedItems.reduce((s, p) => s + (p.item?.price ?? 0), 0).toLocaleString()}
              </span>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default SimulatedARViewer;
