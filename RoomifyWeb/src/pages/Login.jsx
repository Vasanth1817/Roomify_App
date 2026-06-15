import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { loginUser } from '../api/roomifyApi';
import { setCurrentUser } from '../utils/auth';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);

    if (!email || !password) {
      setError('Please enter both email and password.');
      return;
    }

    setLoading(true);
    try {
      const response = await loginUser({ email, password });
      setCurrentUser(response);
      navigate('/');
    } catch (err) {
      setError(err.message || 'Login failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-shell" style={{ padding: '3rem 1.5rem' }}>
      <div className="auth-card glass-panel" style={{ maxWidth: '520px', margin: '0 auto', padding: '2rem' }}>
        <h1 style={{ marginBottom: '1rem' }}>Welcome Back</h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>Log in with your Roomify account to continue designing and saving rooms.</p>
        {error && <div style={{ marginBottom: '1rem', color: '#FF6B6B' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <label style={{ display: 'block', marginBottom: '0.75rem', color: 'var(--text-muted)' }}>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <label style={{ display: 'block', marginBottom: '1rem', color: 'var(--text-muted)' }}>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <button type="submit" disabled={loading} className="btn-primary" style={{ width: '100%', padding: '1rem', borderRadius: '14px', fontSize: '1rem' }}>
            {loading ? 'Signing in…' : 'Sign In'}
          </button>
        </form>

        <p style={{ marginTop: '1.5rem', color: 'var(--text-muted)', textAlign: 'center' }}>
          Don't have an account?{' '}
          <Link to="/register" style={{ color: '#8B4DFA' }}>
            Create one now.
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
