import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import '@google/model-viewer'
import './index.css'
import App from './App.jsx'

if (localStorage.getItem('theme') === 'light') {
  document.body.classList.add('light-mode');
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
