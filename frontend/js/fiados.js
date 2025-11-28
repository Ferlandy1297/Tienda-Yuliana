import { requireAuth, setUserUI, logout } from './auth.js';
import { fiadosApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

const tbody = document.querySelector('#tbl tbody');
async function load(){
  const list = await fiadosApi.clientes();
  tbody.innerHTML='';
  list.forEach(c => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${c.idCliente}</td><td>${c.nombre}</td><td>${c.saldo}</td><td>${c.bloqueado?'Sí':'No'}</td>
      <td class="hstack"><input type="number" step="0.01" min="0.01" style="width:120px" placeholder="Monto"/> <button class="btn" data-abono="${c.idCliente}">Abonar</button></td>`;
    tbody.appendChild(tr);
  });
}
document.getElementById('reload')?.addEventListener('click', load);
load();

tbody?.addEventListener('click', async (e)=>{
  const b = e.target.closest('button'); if (!b) return;
  if (b.dataset.abono) {
    const montoInput = b.parentElement.querySelector('input');
    const monto = String(Number(montoInput.value||'0'));
    if (!monto || Number(monto) <= 0) return alert('Monto inválido');
    try { await fiadosApi.abono({ idCliente: Number(b.dataset.abono), monto }); await load(); }
    catch (err) { alert(err.message); }
  }
});

