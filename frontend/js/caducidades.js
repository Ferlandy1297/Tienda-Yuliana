import { requireAuth, setUserUI, logout } from './auth.js';
import { caducidadesApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

const tbody = document.querySelector('#tbl tbody');

async function load(){
  const dias = Number(document.getElementById('dias').value || '30');
  const lotes = await caducidadesApi.porVencer(dias);
  tbody.innerHTML = '';
  lotes.forEach(l => {
    const tr = document.createElement('tr');
    const p = l.producto || {};
    tr.innerHTML = `<td>${l.id}</td><td>${p.codigoBarras||''}</td><td>${p.nombre||''}</td><td>${l.fechaVencimiento||''}</td><td>${l.cantidad||0}</td><td>${l.estado||''}</td>`;
    tbody.appendChild(tr);
  });
}

document.getElementById('buscar')?.addEventListener('click', load);
load();

