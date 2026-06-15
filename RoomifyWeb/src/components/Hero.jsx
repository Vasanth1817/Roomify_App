import React from 'react';
import { motion } from 'framer-motion';
import { Sparkles, ArrowRight } from 'lucide-react';
import './Hero.css';

const Hero = () => {
  return (
    <section className="hero">
      <div className="hero-background">
        <div className="glow glow-1"></div>
        <div className="glow glow-2"></div>
      </div>
      
      <div className="hero-content">
        <motion.div 
          className="badge"
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.5, delay: 0.2 }}
        >
          <Sparkles size={14} className="badge-icon" />
          <span>Roomify v2.0 is now live</span>
        </motion.div>

        <motion.h1 
          className="title"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.3 }}
        >
          Design your dream space <br/>
          <span className="gradient-text-primary">in Augmented Reality.</span>
        </motion.h1>

        <motion.p 
          className="subtitle"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.4 }}
        >
          Visualize furniture in your actual room before you buy. 
          Use instant themes, track your budget, and compare before/after transformations with pixel-perfect accuracy.
        </motion.p>

        <motion.div 
          className="hero-actions"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7, delay: 0.5 }}
        >
          <button className="btn-primary btn-large">
            Download the App <ArrowRight size={18} />
          </button>
          <button className="btn-secondary btn-large">
            View Live Catalog
          </button>
        </motion.div>
      </div>
    </section>
  );
};

export default Hero;
