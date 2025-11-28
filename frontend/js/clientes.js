import { requireAuth, setUserUI, logout } from './auth.js';
import { clientesApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

const tbody = document.querySelector('#tbl tbody');
const form = document.getElementById('form');
const feedback = document.getElementById('feedback');

async function load(){
  const list = await clientesApi.listar();
  tbody.innerHTML='';
  list.forEach(c => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${c.id}</td><td>${c.nombre}</td><td>${c.telefono||''}</td><td>${c.esMayorista?'Sí':'No'}</td>
    <td class="hstack"><button class="btn" data-edit="${c.id}">Editar</button><button class="btn btn-danger" data-del="${c.id}">Eliminar</button></td>`;
    tbody.appendChild(tr);
  });
}
document.getElementById('reload')?.addEventListener('click', load);
load();

tbody?.addEventListener('click', async (e)=>{
  const b = e.target.closest('button'); if(!b) return;
  if (b.dataset.edit) {
    const id = b.dataset.edit; const c = (await clientesApi.listar()).find(x=> String(x.id)===String(id));
    if (!c) return; 
    form.elements.id.value = c.id; form.elements.nombre.value = c.nombre; form.elements.telefono.value = c.telefono||''; form.elements.esMayorista.value = String(!!c.esMayorista);
    if (form.elements.limiteCredito) form.elements.limiteCredito.value = c.limiteCredito ?? '';
  }
  if (b.dataset.del) {
    if (!confirm('¿Eliminar cliente?')) return;
    await clientesApi.eliminar(b.dataset.del); await load();
  }
});

form?.addEventListener('submit', async (e)=>{
  e.preventDefault(); feedback.textContent=''; feedback.className='';
  const fd = new FormData(form); const dto = Object.fromEntries(fd.entries());
  dto.esMayorista = dto.esMayorista === 'true';
  if (dto.limiteCredito === '') delete dto.limiteCredito; else dto.limiteCredito = String(dto.limiteCredito);
  try {
    if (dto.id) { const id = dto.id; delete dto.id; await clientesApi.actualizar(id, dto); } else { await clientesApi.crear(dto); }
    feedback.textContent='Guardado'; feedback.className='success'; form.reset(); await load();
  } catch (e2) { feedback.textContent=e2.message; feedback.className='error'; }
});

