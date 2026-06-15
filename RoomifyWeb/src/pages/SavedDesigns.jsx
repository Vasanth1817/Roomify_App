import React, { useState, useEffect } from 'react';
import BeforeAfterSlider from '../components/BeforeAfterSlider';
import { getCurrentUser } from '../utils/auth';
import { getLayouts } from '../api/roomifyApi';

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
        const data = await getLayouts(user.user_id);
        setLayouts(Array.isArray(data) ? data : []);
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
        <p style={{ color: '#AAAABC' }}>Please sign in to see your saved room layouts.</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <h2>My Saved Designs</h2>
      <p style={{ color: '#AAAABC', marginBottom: '2rem' }}>Your saved layouts are stored in the same backend as the app.</p>
      {error && <div style={{ marginBottom: '1rem', color: '#FF6B6B' }}>{error}</div>}

      {layouts.length === 0 ? (
        <div style={{ background: 'rgba(255,255,255,0.05)', padding: '3rem', textAlign: 'center', borderRadius: '12px' }}>
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

            const beforeImg = parsedData.before_image || 'https://via.placeholder.com/600x400/1A1A2E/8B4DFA?text=No+Before+Image';
            const afterImg = parsedData.after_image || 'https://via.placeholder.com/600x400/1A1A2E/8B4DFA?text=No+After+Image';
            const mode = parsedData.mode || 'AR';
            const savedDate = layout.created_at ? new Date(layout.created_at).toLocaleDateString() : 'Unknown date';

            return (
              <div key={index} style={{ background: 'rgba(255,255,255,0.05)', borderRadius: '12px', overflow: 'hidden' }}>
                <BeforeAfterSlider beforeImage={beforeImg} afterImage={afterImg} />
                <div style={{ padding: '1.5rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3 style={{ margin: 0 }}>{layout.name || `Design ${layout.id}`}</h3>
                    <span style={{ background: '#8B4DFA', padding: '0.2rem 0.6rem', borderRadius: '4px', fontSize: '0.8rem' }}>{mode} Mode</span>
                  </div>
                  <p style={{ color: '#AAAABC', fontSize: '0.9rem', marginTop: '0.5rem' }}>Saved on: {savedDate}</p>
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
