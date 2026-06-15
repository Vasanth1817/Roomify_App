import React, { useEffect, useState } from 'react';
import { getCurrentUser, clearCurrentUser } from '../utils/auth';
import { getBudget, updateBudget, getLayouts, getFurniture } from '../api/roomifyApi';
import { useNavigate } from 'react-router-dom';

const Budget = () => {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [budget, setBudget] = useState(0);
  const [target, setTarget] = useState('');
  const [spent, setSpent] = useState(0);
  const [projects, setProjects] = useState([]);
  const [message, setMessage] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const loadBudget = async () => {
      try {
        const budgetResult = await getBudget(user.user_id);
        setBudget(budgetResult.max_budget || 0);
        setTarget(String(budgetResult.max_budget || 0));
      } catch (error) {
        console.error(error);
      }
    };

    const loadSpent = async () => {
      try {
        const [layouts, furniture] = await Promise.all([getLayouts(user.user_id), getFurniture()]);
        const priceMap = new Map(furniture.map((item) => [item.model_url, Number(item.price) || 0]));
        let total = 0;
        const detail = [];

        if (Array.isArray(layouts)) {
          layouts.forEach((layout) => {
            let cost = 0;
            try {
              const layoutData = typeof layout.json_data === 'string' ? JSON.parse(layout.json_data) : layout.json_data;
              const items = layoutData.items || [];
              items.forEach((item) => {
                cost += priceMap.get(item.model_url) || 0;
              });
            } catch (error) {
              console.error(error);
            }
            total += cost;
            detail.push({ name: layout.name || `Design ${layout.id}`, cost });
          });
        }

        setSpent(total);
        setProjects(detail);
      } catch (error) {
        console.error(error);
      }
    };

    loadBudget();
    loadSpent();
  }, [navigate, user?.user_id]);

  if (!user) {
    return null;
  }

  const handleSave = async () => {
    setMessage('');
    const numeric = Number(target);
    if (!numeric || numeric < 0) {
      setMessage('Enter a valid budget amount.');
      return;
    }

    setSaving(true);
    try {
      const result = await updateBudget({ user_id: user.user_id, max_budget: numeric });
      setBudget(result.max_budget);
      setMessage('Budget updated successfully.');
    } catch (err) {
      setMessage(err.message || 'Failed to update budget.');
    } finally {
      setSaving(false);
    }
  };

  const remaining = budget - spent;

  return (
    <div style={{ padding: '2rem', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
        <div className="glass-panel" style={{ padding: '2rem', borderRadius: '24px' }}>
          <h1 style={{ marginBottom: '0.5rem' }}>Budget Planner</h1>
          <p style={{ color: 'var(--text-muted)' }}>Track your budget, save a target amount, and compare it against current design costs.</p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem', marginTop: '1.5rem' }}>
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Current target</span>
              <p style={{ marginTop: '0.5rem', fontSize: '2rem', fontWeight: '700' }}>₹{budget.toLocaleString()}</p>
            </div>
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Total spent</span>
              <p style={{ marginTop: '0.5rem', fontSize: '2rem', fontWeight: '700' }}>₹{spent.toLocaleString()}</p>
            </div>
            <div className="glass-panel" style={{ padding: '1.5rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Remaining</span>
              <p style={{ marginTop: '0.5rem', fontSize: '2rem', fontWeight: '700', color: remaining < 0 ? '#EF4444' : '#22C55E' }}>
                ₹{remaining.toLocaleString()}
              </p>
            </div>
          </div>

          <div style={{ marginTop: '2rem', display: 'grid', gridTemplateColumns: '1fr auto', gap: '1rem', alignItems: 'end' }}>
            <label style={{ display: 'block', color: 'var(--text-muted)' }}>
              Enter your target budget
              <input
                type="number"
                value={target}
                onChange={(e) => setTarget(e.target.value)}
                style={{ width: '100%', marginTop: '0.5rem', padding: '1rem', borderRadius: '16px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
              />
            </label>
            <button onClick={handleSave} disabled={saving} className="btn-primary" style={{ height: '56px', borderRadius: '16px' }}>
              {saving ? 'Saving…' : 'Save Budget'}
            </button>
          </div>
          {message && <p style={{ marginTop: '1rem', color: '#8B4DFA' }}>{message}</p>}
        </div>

        <div className="glass-panel" style={{ padding: '2rem', borderRadius: '24px' }}>
          <h2>Design break down</h2>
          <p style={{ color: 'var(--text-muted)', marginBottom: '1.5rem' }}>Budget is calculated from items saved in your layouts.</p>
          <div style={{ display: 'grid', gap: '1rem' }}>
            {projects.length === 0 ? (
              <div style={{ color: 'var(--text-muted)' }}>No saved layouts found yet.</div>
            ) : (
              projects.map((project, index) => (
                <div key={index} style={{ padding: '1rem', borderRadius: '18px', border: '1px solid rgba(255,255,255,0.08)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem' }}>
                    <strong>{project.name}</strong>
                    <span>₹{project.cost.toLocaleString()}</span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Budget;
