import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Home, Layers, Settings, Grid, Monitor, User, DollarSign, Camera } from 'lucide-react';
import { getCurrentUser } from '../utils/auth';
import { useUnityAR } from '../context/UnityARContext';
import './Navbar.css';

const Navbar = () => {
  const location = useLocation();
  const user = getCurrentUser();
  const { selectedFurniture, placements } = useUnityAR();

  // Show a badge on the AR link when an item is selected or items have been placed
  const arBadgeCount = placements.length;
  const arHasItem = !!selectedFurniture;

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          <Monitor className="navbar-logo" />
          <span>Roomify Web</span>
        </Link>
        
        <div className="navbar-links">
          <Link to="/" className={`nav-link ${location.pathname === '/' ? 'active' : ''}`}>
            <Home size={18} />
            <span>Dashboard</span>
          </Link>
          <Link to="/catalog" className={`nav-link ${location.pathname === '/catalog' ? 'active' : ''}`}>
            <Grid size={18} />
            <span>Catalog</span>
          </Link>
          <Link to="/saved" className={`nav-link ${location.pathname === '/saved' ? 'active' : ''}`}>
            <Layers size={18} />
            <span>Saved</span>
          </Link>
          <Link to="/budget" className={`nav-link ${location.pathname === '/budget' ? 'active' : ''}`}>
            <DollarSign size={18} />
            <span>Budget</span>
          </Link>

          {/* ── AR nav link with optional badge ── */}
          <Link
            to="/ar"
            className={`nav-link nav-link-ar ${location.pathname === '/ar' ? 'active' : ''} ${arHasItem ? 'nav-link-ar-ready' : ''}`}
          >
            <Camera size={18} />
            <span>AR Planner</span>
            {arBadgeCount > 0 && (
              <span className="nav-ar-badge">{arBadgeCount}</span>
            )}
          </Link>

          <Link to={user ? '/profile' : '/login'} className={`nav-link ${location.pathname === '/profile' ? 'active' : ''}`}>
            <User size={18} />
            <span>{user ? 'Profile' : 'Login'}</span>
          </Link>
          <Link to="/settings" className={`nav-link ${location.pathname === '/settings' ? 'active' : ''}`}>
            <Settings size={18} />
            <span>Settings</span>
          </Link>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
