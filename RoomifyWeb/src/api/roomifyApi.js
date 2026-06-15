const API_BASE = import.meta.env.VITE_API_BASE || 'https://roomifybackend.onrender.com';

const handleResponse = async (response) => {
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(data?.detail || data?.message || response.statusText || 'API request failed');
  }
  return data;
};

export const getFurniture = async () => {
  const res = await fetch(`${API_BASE}/furniture`);
  return handleResponse(res);
};

export const getLayouts = async (userId) => {
  const url = new URL(`${API_BASE}/get_layouts`);
  if (userId) url.searchParams.append('user_id', userId);
  const res = await fetch(url.toString());
  return handleResponse(res);
};

export const deleteLayout = async (layoutId) => {
  const res = await fetch(`${API_BASE}/delete_layout/${layoutId}`, {
    method: 'DELETE',
  });
  return handleResponse(res);
};

export const saveLayout = async (payload) => {
  const res = await fetch(`${API_BASE}/save_layout`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
};

export const registerUser = async (payload) => {
  const res = await fetch(`${API_BASE}/api/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
};

export const loginUser = async (payload) => {
  const res = await fetch(`${API_BASE}/api/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
};

export const getBudget = async (userId) => {
  const url = new URL(`${API_BASE}/api/budget`);
  url.searchParams.append('user_id', userId);
  const res = await fetch(url.toString());
  return handleResponse(res);
};

export const updateBudget = async (payload) => {
  const res = await fetch(`${API_BASE}/api/budget`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse(res);
};

export const getUserLayoutsCount = async (userId) => {
  const layouts = await getLayouts(userId);
  return Array.isArray(layouts) ? layouts.length : 0;
};
