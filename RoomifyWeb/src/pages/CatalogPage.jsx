import React, { useState, useEffect, useMemo } from 'react';
import { ShoppingCart, Package, Search, Filter } from 'lucide-react';
import '../components/Catalog.css';
import { getFurniture } from '../api/roomifyApi';

const CatalogPage = () => {
  const [furniture, setFurniture] = useState([]);
  const [loading, setLoading] = useState(true);
  const [budget, setBudget] = useState(200000);
  const [spent, setSpent] = useState(0);
  const [selectedModel, setSelectedModel] = useState(null);
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('All');

  useEffect(() => {
    const loadData = async () => {
      try {
        const data = await getFurniture();
        setFurniture(data.map((item) => ({ ...item, price: Number(String(item.price).replace(/\D/g, "")) || 0 })));
      } catch (err) {
        console.error('Failed to fetch furniture:', err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const categories = useMemo(() => {
    const unique = new Set(furniture.map((item) => item.category || '').filter(Boolean));
    return ['All', ...Array.from(unique)];
  }, [furniture]);

  const filteredItems = useMemo(() => {
    return furniture.filter((item) => {
      const matchesCategory = category === 'All' || item.category === category;
      const query = search.toLowerCase();
      const matchesSearch = query.length === 0 || item.name.toLowerCase().includes(query) || item.category.toLowerCase().includes(query);
      return matchesCategory && matchesSearch;
    });
  }, [furniture, category, search]);

  const addToRoom = (item) => {
    setSpent((prev) => prev + item.price);
    setSelectedModel(item);
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem', display: 'grid', gridTemplateColumns: '1fr auto', gap: '1rem' }}>
        <div>
          <h2>Furniture Catalog</h2>
          <p style={{ color: 'var(--text-muted)' }}>Search, filter and preview the same furniture catalog used in the app.</p>
        </div>
        <div style={{ background: 'var(--surface)', padding: '1rem 1.25rem', borderRadius: '16px' }}>
          <h3 style={{ margin: 0, color: spent > budget ? '#ff4d4d' : '#8B4DFA' }}>
            Selected: ₹{spent.toLocaleString()} / ₹{budget.toLocaleString()}
          </h3>
          <input
            type="range"
            min="0"
            max={Math.max(300000, spent + 50000)}
            value={budget}
            onChange={(e) => setBudget(Number(e.target.value))}
            style={{ width: '100%', marginTop: '0.75rem' }}
          />
        </div>
      </div>

      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap', marginBottom: '1.5rem' }}>
        <label className="search-input" style={{ flex: 1 }}>
          <Search size={18} style={{ marginRight: '0.5rem', color: 'var(--text-muted)' }} />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search furniture or category"
            style={{ width: '100%', background: 'transparent', border: 'none', color: 'var(--text-main)', outline: 'none' }}
          />
        </label>

        <div style={{ display: 'flex', gap: '0.75rem', flexWrap: 'wrap' }}>
          {categories.map((cat) => (
            <button
              key={cat}
              type="button"
              onClick={() => setCategory(cat)}
              className={`category-chip ${category === cat ? 'active' : ''}`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {selectedModel && (
        <div style={{ background: 'var(--surface)', padding: '1rem', borderRadius: '12px', marginBottom: '2rem', textAlign: 'center' }}>
          <h3>3D AR Viewer: {selectedModel.name}</h3>
          <p>Preview the selected item in 3D. Use mobile AR mode for compatible devices.</p>
          <model-viewer
            src={selectedModel.model_url}
            alt={selectedModel.name}
            auto-rotate
            camera-controls
            ar
            ar-modes="scene-viewer webxr quick-look"
            style={{ width: '100%', height: '420px', background: 'var(--bg-darker)', borderRadius: '14px' }}
          />
          <button
            className="btn-secondary"
            onClick={() => setSelectedModel(null)}
            style={{ marginTop: '1rem' }}
          >
            Close Preview
          </button>
        </div>
      )}

      {loading ? (
        <p>Loading catalog...</p>
      ) : (
        <div className="catalog-grid">
          {filteredItems.map((item, index) => (
            <div key={index} className="catalog-card glass-panel" style={{ padding: '1rem', borderRadius: '12px' }}>
              <img
                src={item.thumbnail_url || `https://via.placeholder.com/300x200/1A1A2E/8B4DFA?text=${encodeURIComponent(item.name)}`}
                alt={item.name}
                style={{ width: '100%', height: '150px', objectFit: 'cover', borderRadius: '8px' }}
                onError={(e) => { e.target.src = `https://via.placeholder.com/300x200/1A1A2E/8B4DFA?text=${encodeURIComponent(item.name)}` }}
              />
              <div style={{ marginTop: '1rem' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{item.category}</span>
                <h3 style={{ margin: '0.5rem 0' }}>{item.name}</h3>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '0.75rem' }}>
                  <span style={{ fontWeight: '700', color: '#8B4DFA' }}>₹{item.price.toLocaleString()}</span>
                  <div style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-icon" onClick={() => setSelectedModel(item)}>
                      <Package size={16} />
                    </button>
                    <button className="btn-icon" onClick={() => addToRoom(item)} style={{ background: '#8B4DFA' }}>
                      <ShoppingCart size={16} />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default CatalogPage;
