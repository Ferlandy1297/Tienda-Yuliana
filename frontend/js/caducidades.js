import { requireAuth, setUserUI, logout } from './auth.js';
import { caducidadesApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

const tbody = document.querySelector('#tbl tbody');

async function load(){
  const dias = Number(document.getElementById('dias').value || '30');
  const lotes = await caducidadesApi.porVencer(dias);
  tbody.innerHTML = '';
  const role = localStorage.getItem('rol');
  const canActions = role === 'ADMIN' || role === 'SUPERVISOR';
  lotes.forEach(l => {
    const tr = document.createElement('tr');
    const p = l.producto || {};
    if (l.id != null) tr.setAttribute('data-id', String(l.id));
    tr.innerHTML = `<td>${p.codigoBarras||''}</td><td>${p.nombre||''}</td><td>${l.fechaVencimiento||''}</td><td>${l.cantidad||0}</td><td>${l.estado||''}</td>` +
      `<td>`+
      `<button class="btn" data-descuento="${l.id}">Descuento</button> `+
      `<button class="btn" data-donar="${l.id}">Donar</button> `+
      `<button class="btn" data-devolver="${l.id}">Devolver</button>`+
      `</td>`;
    tbody.appendChild(tr);
    if (!canActions) {
      const last = tr.querySelector('td:last-child'); if (last) last.innerHTML = '';
    }
  });
}

document.getElementById('buscar')?.addEventListener('click', load);
load();

// Actions
tbody?.addEventListener('click', async (e)=>{
  const b = e.target.closest('button'); if (!b) return;
  const idLote = Number(b.dataset.descuento || b.dataset.donar || b.dataset.devolver || '0'); if (!idLote) return;
  try {
    if (b.dataset.descuento) {
      const valor = prompt('Valor de descuento (porcentaje 0-100):', '10'); if (!valor) return;
      await caducidadesApi.aplicarDescuento(idLote, { tipo: 'PORCENTAJE', valor: String(Number(valor)), diasVigencia: 7 });
      alert('Promoción aplicada');
    } else if (b.dataset.donar) {
      const cant = prompt('Cantidad a donar:', '1'); if (!cant) return;
      await caducidadesApi.donar(idLote, { cantidad: Number(cant), motivo: 'DONACIÓN' });
      alert('Donación registrada');
    } else if (b.dataset.devolver) {
      const idProveedor = prompt('ID proveedor (nacional):', ''); if (!idProveedor) return;
      const cant = prompt('Cantidad a devolver:', '1'); if (!cant) return;
      await caducidadesApi.devolver(idLote, { idProveedor: Number(idProveedor), cantidad: Number(cant), motivo: 'Caducidad próxima' });
      alert('Devolución registrada');
    }
    await load();
  } catch (err) { alert(err.message); }
});
