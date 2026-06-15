import React from 'react';
import { motion } from 'framer-motion';
import { Box } from 'lucide-react';
import './Header.css';

const Header = () => {
  return (
    <motion.header 
      className="header"
      initial={{ y: -100, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
    >
      <div className="header-container">
        <div className="logo">
          <Box className="logo-icon" size={28} />
          <span className="logo-text">Roomify</span>
        </div>
        
        <nav className="nav-links">
          <a href="#features">Features</a>
          <a href="#catalog">Catalog</a>
          <a href="#download">Download App</a>
        </nav>
        
        <button className="btn-primary">
          Get Started
        </button>
      </div>
    </motion.header>
  );
};

export default Header;
