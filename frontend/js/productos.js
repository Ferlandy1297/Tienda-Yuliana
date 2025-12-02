import { requireAuth, setUserUI, logout, hasRole } from './auth.js';
import { productosApi, proveedoresApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

const tbody = document.querySelector('#tbl tbody');
const feedback = document.getElementById('feedback');
const form = document.getElementById('formProd');

function row(p){
  const tr = document.createElement('tr');
  if (p.id != null) tr.setAttribute('data-id', String(p.id));
  tr.innerHTML = `
    <td>${p.nombre}</td>
    <td>${p.codigoBarras}</td>
    <td>${p.proveedor?.nombre || ''}</td>
    <td>${p.precioUnitario}</td>
    <td>${p.stockActual}</td>
    <td>${p.stockMinimo}</td>
    <td>${p.activo ? 'Sí' : 'No'}</td>
    <td class="hstack">
      <button class="btn" data-edit="${p.id}">Editar</button>
      <button class="btn btn-danger" data-toggle="${p.id}">${p.activo?'Desactivar':'Activar'}</button>
    </td>`;
  return tr;
}

async function load(listFn = productosApi.listar){
  tbody.innerHTML = '';
  const list = await listFn();
  list.forEach(p => tbody.appendChild(row(p)));
  if (!hasRole(['ADMIN','SUPERVISOR'])) {
    tbody.querySelectorAll('button[data-edit], button[data-toggle]')
      .forEach(btn => btn.remove());
    try { document.querySelector('#tbl thead th:last-child')?.classList.add('hidden'); } catch(_) {}
  }
}

document.getElementById('btnReload')?.addEventListener('click', () => load());
document.getElementById('btnLowStock')?.addEventListener('click', () => load(productosApi.stockBajo));

tbody?.addEventListener('click', async (e) => {
  const btn = e.target.closest('button'); if (!btn) return;
  if (btn.dataset.edit) {
    const p = await productosApi.obtener(btn.dataset.edit);
    for (const [k,v] of Object.entries({
      id: p.id, nombre: p.nombre, codigoBarras: p.codigoBarras,
      precioUnitario: p.precioUnitario, stockActual: p.stockActual,
      stockMinimo: p.stockMinimo, idProveedor: p.proveedor?.id
    })) { const el = form.elements[k]; if (el) el.value = v ?? ''; }
    // Asegurar que el select por id también refleje el valor
    const selProv = document.getElementById('proveedorSelect');
    if (selProv) selProv.value = p.proveedor?.id ?? '';
  }
  if (btn.dataset.toggle) {
    if (!confirm('¿Cambiar estado activo?')) return;
    await productosApi.toggle(btn.dataset.toggle);
    await load();
  }
});

document.getElementById('btnClear')?.addEventListener('click', () => { form.reset(); form.elements.id.value=''; });

form?.addEventListener('submit', async (e) => {
  e.preventDefault(); feedback.textContent=''; feedback.className='';
  const fd = new FormData(form);
  const dto = Object.fromEntries(fd.entries());
  ['precioUnitario','stockActual','stockMinimo'].forEach(k => {
    if (dto[k] === '') delete dto[k];
    if (k !== 'precioUnitario' && dto[k] != null) dto[k] = dto[k] === '' ? undefined : Number(dto[k]);
  });
  // Tomar idProveedor explícitamente desde el select por id
  const selProv = document.getElementById('proveedorSelect');
  const idProvVal = selProv ? selProv.value : '';
  if (idProvVal === '') delete dto.idProveedor; else dto.idProveedor = Number(idProvVal);
  if (dto.precioUnitario != null) dto.precioUnitario = String(dto.precioUnitario);
  try {
    if (dto.id) { const id = dto.id; delete dto.id; await productosApi.actualizar(id, dto); }
    else { await productosApi.crear(dto); }
    feedback.textContent = 'Guardado'; feedback.className = 'success';
    form.reset(); await load();
  } catch (err) { feedback.textContent = err.message; feedback.className = 'error'; }
});

async function loadProveedoresSelect(){
  const sel = document.getElementById('proveedorSelect');
  if (!sel) return;
  try {
    const provs = await proveedoresApi.listar();
    sel.innerHTML = '<option value="">Sin proveedor</option>' + provs.map(p=>`<option value="${p.id}">${p.nombre}</option>`).join('');
  } catch (e) { /* noop */ }
}

(async function init(){
  await loadProveedoresSelect();
  await load();
})();
