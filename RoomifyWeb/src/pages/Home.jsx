import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Grid, Layers, Settings, DollarSign, User, ArrowRight } from 'lucide-react';
import { getCurrentUser } from '../utils/auth';
import { getBudget, getLayouts, getFurniture } from '../api/roomifyApi';

const Home = () => {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [budget, setBudget] = useState(0);
  const [spent, setSpent] = useState(0);
  const [savedCount, setSavedCount] = useState(0);
  const [remaining, setRemaining] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadDashboard = async () => {
      if (!user) {
        setLoading(false);
        return;
      }

      try {
        const [budgetData, layouts, furniture] = await Promise.all([
          getBudget(user.user_id),
          getLayouts(user.user_id),
          getFurniture(),
        ]);

        const items = Array.isArray(layouts) ? layouts : [];
        setSavedCount(items.length);

        const priceMap = new Map(furniture.map((item) => [item.model_url, Number(item.price) || 0]));
        let totalSpent = 0;
        for (const layout of items) {
          try {
            const data = typeof layout.json_data === 'string' ? JSON.parse(layout.json_data) : layout.json_data;
            const layoutItems = data.items || [];
            layoutItems.forEach((item) => {
              totalSpent += priceMap.get(item.model_url) || 0;
            });
          } catch (err) {
            console.error('Failed to parse layout', err);
          }
        }

        const currentBudget = budgetData?.max_budget || 0;
        setBudget(currentBudget);
        setSpent(totalSpent);
        setRemaining(currentBudget - totalSpent);
      } catch (err) {
        setError('Unable to load dashboard data.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
  }, [user]);

  if (!user) {
    return (
      <div className="page-shell" style={{ padding: '3rem 1.5rem' }}>
        <div className="glass-panel" style={{ padding: '2.5rem', maxWidth: '920px', margin: '0 auto' }}>
          <div style={{ display: 'grid', gap: '2rem' }}>
            <div>
              <h1>Welcome to Roomify</h1>
              <p style={{ color: 'var(--text-muted)', maxWidth: '720px', marginTop: '1rem' }}>
                The website now matches the app flow. Log in or register to save designs, use the catalog, and manage your budget on the same backend.
              </p>
            </div>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem' }}>
              <Link to="/login" className="btn-primary" style={{ padding: '1rem 1.5rem' }}>
                Sign In
              </Link>
              <Link to="/register" className="btn-secondary" style={{ padding: '1rem 1.5rem' }}>
                Create Account
              </Link>
              <Link to="/catalog" className="btn-secondary" style={{ padding: '1rem 1.5rem' }}>
                View Catalog
              </Link>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '1.5rem', flexWrap: 'wrap' }}>
          <div>
            <p style={{ color: 'var(--text-muted)' }}>Good to see you back,</p>
            <h1 style={{ margin: 0 }}>{user.full_name}</h1>
          </div>
          <button className="btn-secondary" onClick={() => navigate('/profile')} style={{ padding: '0.9rem 1.4rem' }}>
            View Profile
          </button>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1.25rem' }}>
          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <span style={{ color: 'var(--text-muted)' }}>Target budget</span>
            <p style={{ marginTop: '0.75rem', fontSize: '2rem', fontWeight: 700 }}>₹{budget.toLocaleString()}</p>
          </div>
          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <span style={{ color: 'var(--text-muted)' }}>Total spent</span>
            <p style={{ marginTop: '0.75rem', fontSize: '2rem', fontWeight: 700 }}>₹{spent.toLocaleString()}</p>
          </div>
          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <span style={{ color: 'var(--text-muted)' }}>Remaining</span>
            <p style={{ marginTop: '0.75rem', fontSize: '2rem', fontWeight: 700, color: remaining < 0 ? '#EF4444' : '#22C55E' }}>
              ₹{remaining.toLocaleString()}
            </p>
          </div>
          <div className="glass-panel" style={{ padding: '1.75rem' }}>
            <span style={{ color: 'var(--text-muted)' }}>Saved designs</span>
            <p style={{ marginTop: '0.75rem', fontSize: '2rem', fontWeight: 700 }}>{savedCount}</p>
          </div>
        </div>
      </div>

      {error && <div style={{ color: '#FF6B6B', marginBottom: '1rem' }}>{error}</div>}

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1.25rem' }}>
        <Link to="/catalog" className="glass-panel btn-card" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}><Grid size={20} />
            <div>
              <h3>Furniture Catalog</h3>
              <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Browse items and preview them in 3D/AR.</p>
            </div>
          </div>
        </Link>
        <Link to="/saved" className="glass-panel btn-card" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}><Layers size={20} />
            <div>
              <h3>Saved Designs</h3>
              <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Open the same saved layouts used by the app.</p>
            </div>
          </div>
        </Link>
        <Link to="/budget" className="glass-panel btn-card" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}><DollarSign size={20} />
            <div>
              <h3>Budget Planner</h3>
              <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>Update your target budget in the shared backend.</p>
            </div>
          </div>
        </Link>
        <Link to="/profile" className="glass-panel btn-card" style={{ padding: '2rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}><User size={20} />
            <div>
              <h3>My Profile</h3>
              <p style={{ color: 'var(--text-muted)', marginTop: '0.5rem' }}>See account details and saved layout stats.</p>
            </div>
          </div>
        </Link>
      </div>
    </div>
  );
};

export default Home;
