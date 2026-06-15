import React, { useState, useEffect } from 'react';
import { Moon, Sun, Bell, Monitor, Wrench } from 'lucide-react';

const Settings = () => {
  const [darkMode, setDarkMode] = useState(true);

  useEffect(() => {
    if (darkMode) {
      document.body.classList.remove('light-mode');
    } else {
      document.body.classList.add('light-mode');
    }
  }, [darkMode]);

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <h2>Settings</h2>
      
      <div style={{ background: 'rgba(255,255,255,0.05)', padding: '2rem', borderRadius: '12px', marginTop: '2rem' }}>
        <h3 style={{ borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '1rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Monitor size={20} color="#8B4DFA" /> Appearance
        </h3>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 0' }}>
          <div>
            <h4 style={{ margin: 0 }}>Dark Mode</h4>
            <p style={{ margin: 0, color: '#AAAABC', fontSize: '0.9rem' }}>Toggle between dark and light themes</p>
          </div>
          <button 
            onClick={() => setDarkMode(!darkMode)}
            style={{ 
              background: darkMode ? '#8B4DFA' : '#ccc', 
              border: 'none', 
              padding: '0.5rem 1rem', 
              borderRadius: '20px', 
              color: 'white', 
              cursor: 'pointer',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem'
            }}
          >
            {darkMode ? <Moon size={16} /> : <Sun size={16} />}
            {darkMode ? 'Enabled' : 'Disabled'}
          </button>
        </div>
      </div>

      <div style={{ background: 'rgba(255,255,255,0.05)', padding: '2rem', borderRadius: '12px', marginTop: '2rem' }}>
        <h3 style={{ borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '1rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Bell size={20} color="#8B4DFA" /> Notifications
        </h3>
        
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1rem 0' }}>
          <div>
            <h4 style={{ margin: 0 }}>Push Notifications</h4>
            <p style={{ margin: 0, color: '#AAAABC', fontSize: '0.9rem' }}>Receive alerts about budget limits and designs</p>
          </div>
          <input type="checkbox" defaultChecked style={{ width: '20px', height: '20px', accentColor: '#8B4DFA' }} />
        </div>
      </div>

      <div style={{ background: 'rgba(255,255,255,0.05)', padding: '2rem', borderRadius: '12px', marginTop: '2rem' }}>
        <h3 style={{ borderBottom: '1px solid rgba(255,255,255,0.1)', paddingBottom: '1rem', marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Wrench size={20} color="#8B4DFA" /> Maintenance
        </h3>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <button style={{ background: 'transparent', border: '1px solid #ff4d4d', color: '#ff4d4d', padding: '1rem', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontWeight: 'bold' }}>
            Clear Local Cache
          </button>
          <button style={{ background: '#ff4d4d', border: 'none', color: 'white', padding: '1rem', borderRadius: '8px', cursor: 'pointer', textAlign: 'left', fontWeight: 'bold' }}>
            Reset All Designs
          </button>
        </div>
      </div>
    </div>
  );
};

export default Settings;
