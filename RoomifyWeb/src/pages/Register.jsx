import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../api/roomifyApi';
import { setCurrentUser } from '../utils/auth';

const Register = () => {
  const navigate = useNavigate();
  const [fullName, setFullName] = useState('');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);

    if (!fullName || !email || !password || !confirmPassword) {
      setError('Please fill in all required fields.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    try {
      const response = await registerUser({
        full_name: fullName,
        phone_number: phoneNumber,
        email,
        password,
      });
      setCurrentUser({ user_id: response.user_id, full_name: fullName, email });
      navigate('/');
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-shell" style={{ padding: '3rem 1.5rem' }}>
      <div className="auth-card glass-panel" style={{ maxWidth: '620px', margin: '0 auto', padding: '2rem' }}>
        <h1 style={{ marginBottom: '1rem' }}>Create your account</h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>Register once and save your room layouts in the same backend used by the app.</p>
        {error && <div style={{ marginBottom: '1rem', color: '#FF6B6B' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <label style={{ display: 'block', marginBottom: '0.8rem', color: 'var(--text-muted)' }}>
            Full Name
            <input
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="John Doe"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <label style={{ display: 'block', marginBottom: '0.8rem', color: 'var(--text-muted)' }}>
            Phone Number
            <input
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              placeholder="+91 98765 43210"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <label style={{ display: 'block', marginBottom: '0.8rem', color: 'var(--text-muted)' }}>
            Email
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <label style={{ display: 'block', marginBottom: '0.8rem', color: 'var(--text-muted)' }}>
            Password
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Create a password"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <label style={{ display: 'block', marginBottom: '1rem', color: 'var(--text-muted)' }}>
            Confirm Password
            <input
              type="password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="Repeat your password"
              style={{ width: '100%', marginTop: '0.5rem', padding: '0.9rem 1rem', borderRadius: '12px', border: '1px solid rgba(255,255,255,0.12)', background: 'var(--surface)', color: 'var(--text-main)' }}
            />
          </label>

          <button type="submit" disabled={loading} className="btn-primary" style={{ width: '100%', padding: '1rem', borderRadius: '14px', fontSize: '1rem' }}>
            {loading ? 'Creating account…' : 'Register'}
          </button>
        </form>

        <p style={{ marginTop: '1.5rem', color: 'var(--text-muted)', textAlign: 'center' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: '#8B4DFA' }}>
            Sign in.
          </Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
