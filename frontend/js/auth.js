import { login } from './api.js';

export function requireAuth() {
  const token = localStorage.getItem('token');
  if (!token) {
    // allow index.html unauthenticated; protect others
    if (!location.pathname.endsWith('/index.html')) {
      location.href = './index.html';
    }
  }
}

export function setUserUI() {
  const u = localStorage.getItem('username');
  const r = localStorage.getItem('rol');
  const elU = document.querySelector('[data-username]');
  const elR = document.querySelector('[data-rol]');
  if (elU) elU.textContent = u || '';
  if (elR) elR.textContent = r || '';
  // Hide UI elements based on required roles, if any
  document.querySelectorAll('[data-requires-role]')?.forEach(el => {
    const allowed = (el.getAttribute('data-requires-role') || '').split(',').map(s => s.trim()).filter(Boolean);
    if (allowed.length && !allowed.includes(r)) el.classList.add('hidden');
  });
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('rol');
  localStorage.removeItem('username');
  location.href = './index.html';
}

export async function handleLoginForm(form, feedbackEl) {
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fd = new FormData(form);
    const username = fd.get('username');
    const password = fd.get('password');
    feedbackEl.textContent = '';
    try {
      const res = await login(username, password);
      localStorage.setItem('token', res.token);
      localStorage.setItem('rol', res.rol);
      localStorage.setItem('username', res.username);
      location.href = './dashboard.html';
    } catch (err) {
      feedbackEl.textContent = err.message || 'Error de autenticación';
      feedbackEl.classList.add('error');
    }
  });
}

export function guardByRole(roles /* array of strings */) {
  const r = localStorage.getItem('rol');
  if (!r || !roles.includes(r)) {
    // render-only guard: hide restricted sections
    document.querySelectorAll('[data-requires-role]').forEach(el => {
      const allowed = (el.getAttribute('data-requires-role') || '').split(',').map(s => s.trim()).filter(Boolean);
      if (allowed.length && !allowed.includes(r)) el.classList.add('hidden');
    });
  }
}

export function hasRole(role) {
  const r = localStorage.getItem('rol');
  return r === role || (Array.isArray(role) && role.includes(r));
}

