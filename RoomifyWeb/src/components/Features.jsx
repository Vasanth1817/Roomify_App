import React from 'react';
import { motion } from 'framer-motion';
import { Layers, GitCompare, Camera } from 'lucide-react';
import './Features.css';

const featuresData = [
  {
    icon: <Camera size={24} />,
    title: 'AR Furniture Placement',
    description: 'Drop 3D furniture perfectly into your room. We fixed the AR tracking so your saved designs spawn accurately in front of you!'
  },
  {
    icon: <Layers size={24} />,
    title: 'Instant Themes',
    description: 'Transform an empty room in seconds. Apply pre-designed themes like Minimalist or Zen directly into your physical space.'
  },
  {
    icon: <GitCompare size={24} />,
    title: 'Before / After Sliders',
    description: 'Capture the transformation. Our dynamic slider lets you swipe back and forth to see exactly what you changed.'
  }
];

const Features = () => {
  return (
    <section id="features" className="features-section">
      <div className="features-container">
        <div className="features-header">
          <h2 className="section-title">The Future of <span className="gradient-text-primary">Interior Design</span></h2>
          <p className="section-subtitle">Everything you need to visualize and plan your perfect space, powered by cutting-edge Augmented Reality.</p>
        </div>

        <div className="features-grid">
          {featuresData.map((feature, index) => (
            <motion.div 
              key={index}
              className="feature-card glass-panel"
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-100px" }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              whileHover={{ y: -5 }}
            >
              <div className="feature-icon-wrapper">
                {feature.icon}
              </div>
              <h3 className="feature-title">{feature.title}</h3>
              <p className="feature-description">{feature.description}</p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

export default Features;
