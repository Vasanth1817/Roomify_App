import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { ShoppingCart } from 'lucide-react';
import './Catalog.css';
import { getFurniture } from '../api/roomifyApi';

const Catalog = () => {
  const [furniture, setFurniture] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchCatalog = async () => {
      try {
        const data = await getFurniture();
        setFurniture(data);
      } catch (err) {
        console.error("Failed to fetch furniture:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchCatalog();
  }, []);

  return (
    <section id="catalog" className="catalog-section">
      <div className="catalog-container">
        <div className="catalog-header">
          <h2 className="section-title">Live 3D <span className="gradient-text-primary">Catalog</span></h2>
          <p className="section-subtitle">Explore our collection of high-quality 3D models available instantly in the AR app.</p>
        </div>

        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading catalog from backend...</p>
          </div>
        ) : (
          <div className="catalog-grid">
            {furniture.map((item, index) => (
              <motion.div 
                key={index}
                className="catalog-card glass-panel"
                initial={{ opacity: 0, scale: 0.95 }}
                whileInView={{ opacity: 1, scale: 1 }}
                viewport={{ once: true }}
                transition={{ duration: 0.4, delay: index * 0.05 }}
              >
                <div className="catalog-image-wrapper">
                  {/* Using a placeholder if thumbnail_url is missing or invalid */}
                  <img 
                    src={item.thumbnail_url || `https://via.placeholder.com/300x200/1A1A2E/8B4DFA?text=${item.name.replace(' ', '+')}`} 
                    alt={item.name} 
                    className="catalog-image"
                    onError={(e) => { e.target.src = `https://via.placeholder.com/300x200/1A1A2E/8B4DFA?text=${item.name.replace(' ', '+')}`}}
                  />
                  <div className="category-tag">{item.category}</div>
                </div>
                
                <div className="catalog-details">
                  <h3 className="item-name">{item.name}</h3>
                  <div className="item-bottom-row">
                    <span className="item-price">₹{item.price.toLocaleString()}</span>
                    <button className="btn-icon">
                      <ShoppingCart size={18} />
                    </button>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default Catalog;
