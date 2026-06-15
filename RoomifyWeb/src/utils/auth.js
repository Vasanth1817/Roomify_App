const STORAGE_KEY = 'roomify_user_session';

export const getCurrentUser = () => {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
};

export const setCurrentUser = (user) => {
  if (!user) return;
  localStorage.setItem(STORAGE_KEY, JSON.stringify(user));
};

export const clearCurrentUser = () => {
  localStorage.removeItem(STORAGE_KEY);
};

export const isLoggedIn = () => {
  return Boolean(getCurrentUser()?.user_id);
};

export const getUserId = () => {
  return getCurrentUser()?.user_id || '';
};
