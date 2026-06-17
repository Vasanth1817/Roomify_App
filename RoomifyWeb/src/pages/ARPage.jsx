import React, { useState, useCallback, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Camera, CheckCircle, Package, Trash2, RotateCcw, Box, Info,
  Zap, Globe,
} from 'lucide-react';
import { useUnityAR } from '../context/UnityARContext';
import UnityARViewer from '../components/UnityARViewer';

/* ── stat card ────────────────────────────────────────────────── */
const StatCard = ({ label, value, sub, color }) => (
  <div className="glass-panel" style={{ padding: '1.25rem 1.5rem' }}>
    <span style={{ color: 'var(--text-muted)', fontSize: '0.82rem' }}>{label}</span>
    <p style={{ margin: '0.5rem 0 0', fontSize: '1.6rem', fontWeight: 700, color: color ?? 'inherit' }}>
      {value}
    </p>
    {sub && <p style={{ margin: '0.2rem 0 0', fontSize: '0.78rem', color: 'var(--text-muted)' }}>{sub}</p>}
  </div>
);

/* ── mode badge ───────────────────────────────────────────────── */
const ModeBadge = ({ hasUnity }) => (
  <div style={{
    display: 'inline-flex', alignItems: 'center', gap: '0.45rem',
    padding: '0.3rem 0.8rem',
    borderRadius: '20px',
    background: hasUnity
      ? 'rgba(34,197,94,0.12)' : 'rgba(255,165,0,0.1)',
    border: `1px solid ${hasUnity ? 'rgba(34,197,94,0.3)' : 'rgba(255,165,0,0.25)'}`,
    color: hasUnity ? '#6ee7a0' : '#ffb347',
    fontSize: '0.75rem',
    fontWeight: 700,
  }}>
    {hasUnity ? <Zap size={13} /> : <Globe size={13} />}
    {hasUnity ? 'Unity AR Engine Ready' : 'Browser Demo Mode (Camera + Canvas AR)'}
  </div>
);

/* ═══════════════════════════════════════════════════════════════ */
const ARPage = () => {
  const navigate = useNavigate();
  const {
    selectedFurniture,
    setSelectedFurniture,
    placements,
    setIsARActive,
    clearPlacements,
  } = useUnityAR();

  const [showViewer, setShowViewer]   = useState(false);
  const [hasUnity, setHasUnity]       = useState(null);

  /* probe for Unity build files */
  useEffect(() => {
    fetch('/unity/RoomifyAR.loader.js', { method: 'HEAD' })
      .then(r => setHasUnity(r.ok))
      .catch(() => setHasUnity(false));
  }, []);

  const openAR = useCallback(() => {
    setShowViewer(true);
    setIsARActive(true);
  }, [setIsARActive]);

  const closeAR = useCallback(() => {
    setShowViewer(false);
    setIsARActive(false);
  }, [setIsARActive]);

  const clearSelection = useCallback(() => {
    setSelectedFurniture(null);
    setIsARActive(false);
  }, [setSelectedFurniture, setIsARActive]);

  const totalCost = placements.reduce((acc, p) => acc + (p.item?.price ?? 0), 0);

  return (
    <div style={{ padding: '2rem', maxWidth: '1100px', margin: '0 auto' }}>

      {/* ── header ── */}
      <motion.div
        initial={{ opacity: 0, y: -14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
        style={{ marginBottom: '0.75rem' }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap', marginBottom: '0.6rem' }}>
          <h1 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
            <Camera size={28} style={{ color: '#8B4DFA' }} />
            AR Room Planner
          </h1>
          {hasUnity !== null && <ModeBadge hasUnity={hasUnity} />}
        </div>
        <p style={{ color: 'var(--text-muted)', marginTop: '0.3rem' }}>
          {hasUnity
            ? 'Unity WebGL AR engine loaded. Place real-scale furniture using your camera.'
            : 'Running in browser demo mode — live camera feed with canvas furniture overlay. Select an item from the catalog, then tap the camera view to place it.'}
        </p>
      </motion.div>

      {/* ── stats ── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '1rem', marginBottom: '2rem' }}>
        <StatCard label="Items placed" value={placements.length} />
        <StatCard
          label="Session cost"
          value={`₹${totalCost.toLocaleString()}`}
          color={totalCost > 0 ? '#8B4DFA' : undefined}
        />
        <StatCard
          label="Selected item"
          value={selectedFurniture ? selectedFurniture.name : '—'}
          sub={selectedFurniture ? `₹${(selectedFurniture.price ?? 0).toLocaleString()}` : 'None selected'}
        />
      </div>

      {/* ── launch panel ── */}
      <motion.div
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.1 }}
        className="glass-panel"
        style={{ padding: '2.5rem', marginBottom: '2rem', textAlign: 'center' }}
      >
        {selectedFurniture ? (
          <>
            {/* furniture thumbnail if available */}
            {selectedFurniture.thumbnail_url ? (
              <img
                src={selectedFurniture.thumbnail_url}
                alt={selectedFurniture.name}
                style={{
                  width: 90, height: 90, borderRadius: '18px',
                  objectFit: 'cover', margin: '0 auto 1.25rem',
                  border: '2px solid rgba(139,77,250,0.3)',
                  display: 'block',
                }}
                onError={e => { e.target.style.display = 'none'; }}
              />
            ) : (
              <div style={{
                width: 72, height: 72, borderRadius: '18px',
                background: 'linear-gradient(135deg,rgba(139,77,250,0.2),rgba(139,77,250,0.05))',
                border: '1px solid rgba(139,77,250,0.3)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
                margin: '0 auto 1.25rem',
              }}>
                <Package size={32} style={{ color: '#8B4DFA' }} />
              </div>
            )}

            <h2 style={{ margin: '0 0 0.4rem' }}>{selectedFurniture.name}</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: '0.3rem' }}>
              {selectedFurniture.category}
            </p>
            <p style={{ fontSize: '1.3rem', fontWeight: 700, color: '#8B4DFA', marginBottom: '2rem' }}>
              ₹{(selectedFurniture.price ?? 0).toLocaleString()}
            </p>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <motion.button
                whileHover={{ scale: 1.03 }}
                whileTap={{ scale: 0.97 }}
                onClick={openAR}
                style={{
                  padding: '0.9rem 2.2rem',
                  fontSize: '1rem',
                  background: 'linear-gradient(135deg, #8B4DFA, #6C2FD9)',
                  border: 'none',
                  borderRadius: '14px',
                  color: '#fff',
                  fontWeight: 700,
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.5rem',
                }}
              >
                <Camera size={18} />
                {hasUnity ? 'Launch Unity AR' : '🥽 Launch AR Camera'}
              </motion.button>
              <button
                className="btn-secondary"
                style={{ padding: '0.9rem 1.5rem' }}
                onClick={clearSelection}
              >
                <RotateCcw size={15} style={{ marginRight: '0.4rem' }} />
                Clear Selection
              </button>
            </div>
          </>
        ) : (
          <>
            <div style={{
              width: 72, height: 72, borderRadius: '18px',
              background: 'rgba(255,255,255,0.03)',
              border: '1px solid rgba(255,255,255,0.08)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              margin: '0 auto 1.25rem',
            }}>
              <Box size={32} style={{ color: '#6b7280' }} />
            </div>
            <h2 style={{ margin: '0 0 0.6rem', color: '#9ca3af' }}>No Furniture Selected</h2>
            <p style={{ color: 'var(--text-muted)', maxWidth: '460px', margin: '0 auto 2rem', lineHeight: 1.6 }}>
              Browse the catalog, choose any item, and click the{' '}
              <span style={{ color: '#8B4DFA', fontWeight: 700 }}>🥽 View in AR</span>{' '}
              button to launch the AR camera here.
            </p>

            <div style={{ display: 'flex', gap: '1rem', justifyContent: 'center', flexWrap: 'wrap' }}>
              <motion.button
                whileHover={{ scale: 1.03 }}
                whileTap={{ scale: 0.97 }}
                className="btn-primary"
                style={{ padding: '0.9rem 2rem', fontSize: '0.95rem' }}
                onClick={() => navigate('/catalog')}
              >
                Browse Furniture Catalog →
              </motion.button>
              {/* Allow launching even without selection for camera preview */}
              <button
                className="btn-secondary"
                style={{ padding: '0.9rem 1.5rem', fontSize: '0.9rem' }}
                onClick={openAR}
              >
                Open Camera Preview
              </button>
            </div>
          </>
        )}
      </motion.div>

      {/* ── placement history ── */}
      <AnimatePresence>
        {placements.length > 0 && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="glass-panel"
            style={{ padding: '1.5rem', marginBottom: '1.5rem' }}
          >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.2rem' }}>
              <h3 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <CheckCircle size={18} style={{ color: '#22C55E' }} />
                Placed Items ({placements.length})
              </h3>
              <button
                className="btn-icon"
                onClick={clearPlacements}
                title="Clear all placements"
                style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', padding: '0.4rem 0.8rem', fontSize: '0.8rem', width: 'auto' }}
              >
                <Trash2 size={14} />
                Clear All
              </button>
            </div>

            <div style={{ display: 'grid', gap: '0.65rem' }}>
              {placements.map((p, i) => (
                <motion.div
                  key={i}
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: i * 0.04 }}
                  style={{
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    background: 'rgba(139,77,250,0.06)',
                    border: '1px solid rgba(139,77,250,0.13)',
                    padding: '0.75rem 1rem',
                    borderRadius: '10px',
                    gap: '1rem',
                    flexWrap: 'wrap',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', minWidth: 0 }}>
                    <CheckCircle size={14} style={{ color: '#22C55E', flexShrink: 0 }} />
                    <span style={{ fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {p.item?.name ?? 'Unknown item'}
                    </span>
                    <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>
                      {p.item?.category}
                    </span>
                  </div>
                  <div style={{ display: 'flex', gap: '1.25rem', flexShrink: 0, fontSize: '0.78rem', color: 'var(--text-muted)' }}>
                    <span style={{ color: '#8B4DFA', fontWeight: 700 }}>
                      ₹{(p.item?.price ?? 0).toLocaleString()}
                    </span>
                    <span title="AR world coordinates">
                      x:{(p.x ?? 0).toFixed(2)} z:{(p.z ?? 0).toFixed(2)}
                    </span>
                  </div>
                </motion.div>
              ))}
            </div>

            <div style={{
              marginTop: '1rem', paddingTop: '1rem',
              borderTop: '1px solid rgba(255,255,255,0.06)',
              display: 'flex', justifyContent: 'flex-end',
              alignItems: 'center', gap: '0.5rem',
            }}>
              <span style={{ color: 'var(--text-muted)' }}>Session total:</span>
              <span style={{ fontWeight: 800, fontSize: '1.15rem', color: '#8B4DFA' }}>
                ₹{totalCost.toLocaleString()}
              </span>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ── info note ── */}
      <div style={{
        padding: '1rem 1.25rem',
        background: 'rgba(139,77,250,0.05)',
        border: '1px solid rgba(139,77,250,0.14)',
        borderRadius: '12px',
        display: 'flex', gap: '0.75rem', alignItems: 'flex-start',
      }}>
        <Info size={16} style={{ color: '#8B4DFA', marginTop: '0.15rem', flexShrink: 0 }} />
        <p style={{ margin: 0, fontSize: '0.82rem', color: 'var(--text-muted)', lineHeight: 1.6 }}>
          {hasUnity
            ? <><strong style={{ color: '#b07cf8' }}>Unity AR Engine:</strong> Full WebXR AR active. Tap surfaces to place furniture in real space. Requires HTTPS and WebXR-capable browser (Chrome 90+ on Android / Safari 16+).</>
            : <><strong style={{ color: '#b07cf8' }}>Browser Demo Mode:</strong> Uses your device camera as live background. Tap anywhere to place furniture overlays. Placement coordinates are relative to the canvas. Export Unity WebGL and copy files to <code style={{ color: '#c084fc' }}>public/unity/</code> to upgrade to full Unity AR.</>
          }
        </p>
      </div>

      {/* ── Unity/Simulated AR overlay ── */}
      <AnimatePresence>
        {showViewer && <UnityARViewer onClose={closeAR} />}
      </AnimatePresence>
    </div>
  );
};

export default ARPage;
