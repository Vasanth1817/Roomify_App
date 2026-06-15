import React, { useState, useEffect } from 'react';
import BeforeAfterSlider from '../components/BeforeAfterSlider';
import { getCurrentUser } from '../utils/auth';
import { getLayouts, getFurniture } from '../api/roomifyApi';

const SavedDesigns = () => {
  const [layouts, setLayouts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const user = getCurrentUser();

  useEffect(() => {
    const loadLayouts = async () => {
      if (!user) {
        setLayouts([]);
        setLoading(false);
        return;
      }

      try {
        const [layoutsData, furnitureData] = await Promise.all([
          getLayouts(user.user_id),
          getFurniture()
        ]);
        
        const priceMap = new Map((furnitureData || []).map((item) => [
          item.model_url, 
          Number(String(item.price).replace(/\D/g, "")) || 0
        ]));

        const layoutsArr = Array.isArray(layoutsData) ? layoutsData : [];
        
        const enhancedLayouts = layoutsArr.map(layout => {
          let cost = 0;
          try {
            const parsedData = typeof layout.json_data === 'string' ? JSON.parse(layout.json_data) : layout.json_data;
            const items = parsedData?.items || [];
            items.forEach(item => {
              cost += priceMap.get(item.model_url) || 0;
            });
          } catch (e) {
            console.error(e);
          }
          return { ...layout, total_cost: cost };
        });

        setLayouts(enhancedLayouts);
      } catch (err) {
        console.error('Failed to fetch layouts:', err);
        setError('Unable to load saved designs.');
      } finally {
        setLoading(false);
      }
    };

    loadLayouts();
  }, [user]);

  if (loading) {
    return (
      <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <h2>My Saved Designs</h2>
        <p>Loading your amazing designs...</p>
      </div>
    );
  }

  if (!user) {
    return (
      <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
        <h2>My Saved Designs</h2>
        <p style={{ color: 'var(--text-muted)' }}>Please sign in to see your saved room layouts.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <h2>My Saved Designs</h2>
      <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>Your saved layouts are stored in the same backend as the app.</p>
      {error && <div style={{ marginBottom: '1rem', color: '#FF6B6B' }}>{error}</div>}

      {layouts.length === 0 ? (
        <div style={{ background: 'var(--surface)', padding: '3rem', textAlign: 'center', borderRadius: '12px' }}>
          <p>No saved designs found yet. Go to the Catalog to create one!</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(400px, 1fr))', gap: '2rem' }}>
          {layouts.map((layout, index) => {
            let parsedData = {};
            try {
              if (typeof layout.json_data === 'string') {
                parsedData = JSON.parse(layout.json_data);
              } else {
                parsedData = layout.json_data;
              }
            } catch (e) {
              console.error(e);
            }

            let beforeImg = layout.before_image;
            if (beforeImg && !beforeImg.startsWith('http') && !beforeImg.startsWith('data:image')) {
              beforeImg = `data:image/png;base64,${beforeImg}`;
            }
            if (!beforeImg) beforeImg = 'https://via.placeholder.com/600x400/1A1A2E/8B4DFA?text=No+Before+Image';

            let afterImg = layout.after_image;
            if (afterImg && !afterImg.startsWith('http') && !afterImg.startsWith('data:image')) {
              afterImg = `data:image/png;base64,${afterImg}`;
            }
            if (!afterImg) afterImg = 'https://via.placeholder.com/600x400/1A1A2E/8B4DFA?text=No+After+Image';

            const mode = layout.mode || 'AR';
            const savedDate = layout.created_at ? new Date(layout.created_at).toLocaleDateString() : '';

            return (
              <div key={index} style={{ background: 'var(--surface)', borderRadius: '12px', overflow: 'hidden' }}>
                <BeforeAfterSlider beforeImage={beforeImg} afterImage={afterImg} />
                <div style={{ padding: '1.5rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3 style={{ margin: 0 }}>{layout.name || `Design ${layout.id}`}</h3>
                    <span style={{ background: '#8B4DFA', color: 'white', padding: '0.2rem 0.6rem', borderRadius: '4px', fontSize: '0.8rem' }}>{mode} Mode</span>
                  </div>
                  {savedDate && (
                    <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginTop: '0.5rem' }}>Saved on: {savedDate}</p>
                  )}
                  <p style={{ color: 'var(--text-main)', fontWeight: 'bold', marginTop: '0.75rem' }}>Total Cost: ₹{layout.total_cost.toLocaleString()}</p>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default SavedDesigns;
