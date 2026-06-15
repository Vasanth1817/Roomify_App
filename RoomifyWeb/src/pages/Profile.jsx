import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser, clearCurrentUser } from '../utils/auth';
import { getLayouts } from '../api/roomifyApi';

const Profile = () => {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [savedCount, setSavedCount] = useState(0);
  const [activeRooms, setActiveRooms] = useState(0);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const loadStats = async () => {
      try {
        const layouts = await getLayouts(user.user_id);
        if (Array.isArray(layouts)) {
          setSavedCount(layouts.length);
          setActiveRooms(layouts.filter((item) => item.mode === 'Virtual' || item.mode === 'AR').length);
        }
      } catch (error) {
        console.error(error);
      }
    };

    loadStats();
  }, [navigate, user]);

  if (!user) {
    return null;
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '960px', margin: '0 auto' }}>
      <div className="glass-panel" style={{ padding: '2rem', borderRadius: '24px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: '2rem', flexWrap: 'wrap' }}>
          <div>
            <h1 style={{ marginBottom: '0.5rem' }}>{user.full_name}</h1>
            <p style={{ color: 'var(--text-muted)' }}>{user.email}</p>
          </div>
          <button
            onClick={() => {
              clearCurrentUser();
              navigate('/login');
            }}
            className="btn-secondary"
            style={{ height: '48px', padding: '0 1.5rem', borderRadius: '999px' }}
          >
            Sign Out
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.5rem', marginTop: '2rem' }}>
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <h4>Saved Designs</h4>
            <p style={{ marginTop: '0.5rem', fontSize: '1.9rem', fontWeight: 700 }}>{savedCount}</p>
          </div>
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <h4>Active Rooms</h4>
            <p style={{ marginTop: '0.5rem', fontSize: '1.9rem', fontWeight: 700 }}>{activeRooms}</p>
          </div>
          <div className="glass-panel" style={{ padding: '1.5rem' }}>
            <h4>Account Type</h4>
            <p style={{ marginTop: '0.5rem', fontSize: '1.9rem', fontWeight: 700 }}>Standard</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
