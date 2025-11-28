import { requireAuth, setUserUI, logout } from './auth.js';
import { getDashboardSummary, productosApi } from './api.js';

requireAuth();
setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

(async function load(){
  try {
    const s = await getDashboardSummary();
    const set = (id, v) => { const el = document.getElementById(id); if (el) el.textContent = v ?? '-'; };
    set('mVentas', s.ventasHoy ?? 0);
    set('mProductos', s.productos ?? '-');
    set('mLowStock', s.lowStock ?? '-');
    // list a few low-stock items
    const low = await productosApi.stockBajo();
    const ul = document.getElementById('lowList');
    if (ul) {
      ul.innerHTML = '';
      (low || []).slice(0, 8).forEach(p => {
        const li = document.createElement('li');
        li.textContent = `${p.nombre} · ${p.stockActual}/${p.stockMinimo}`;
        ul.appendChild(li);
      });
      if (!low || !low.length) ul.innerHTML = '<li>Sin alertas</li>';
    }
  } catch (e) { console.error(e); }
})();
