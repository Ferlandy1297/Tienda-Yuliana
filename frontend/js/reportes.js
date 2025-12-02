import { requireAuth, setUserUI, logout } from './auth.js';
import { reportesApi, downloadResponse, proveedoresApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

function todayISO(){ const d = new Date(); return d.toISOString().slice(0,10); }
const fechaEl = document.getElementById('fecha'); if (fechaEl) fechaEl.value = todayISO();

async function consultar(){
  const tipo = document.getElementById('tipo').value; const fecha = document.getElementById('fecha').value;
  const rpt = await reportesApi.ventas(tipo, fecha);
  const resVentas = document.getElementById('resVentas');
  resVentas.innerHTML = `
    <div class="metric-grid">
      <div class="metric"><h3>Periodo</h3><div class="value">${rpt.inicio} → ${rpt.fin}</div></div>
      <div class="metric"><h3>Total ventas</h3><div class="value">${rpt.total}</div></div>
      <div class="metric"><h3>Transacciones</h3><div class="value">${rpt.transacciones}</div></div>
    </div>`;

  const mas = await reportesApi.masVendidos(tipo, fecha);
  const tbody = document.querySelector('#tblMas tbody'); tbody.innerHTML = '';
  mas.forEach(m => {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td>${m.nombre}</td><td>${m.cantidad}</td><td>${m.total}</td>`; tbody.appendChild(tr);
  });
  // Draw simple bar chart without external libs
  const canvas = document.getElementById('chartMasVendidos');
  if (canvas) {
    const ctx = canvas.getContext('2d');
    const W = canvas.width = canvas.clientWidth || 400; const H = canvas.height = canvas.clientHeight || 200;
    ctx.clearRect(0,0,W,H);
    const values = mas.map(x => Number(x.cantidad)||0);
    const labels = mas.map(x => String(x.nombre||''));
    const max = Math.max(1, ...values);
    const pad = 24; const bw = Math.max(10, (W - pad*2) / (values.length || 1) - 8);
    ctx.fillStyle = '#64748b'; ctx.fillRect(0,0,W,H);
    // axes
    ctx.strokeStyle = '#94a3b8'; ctx.beginPath(); ctx.moveTo(pad, H-pad); ctx.lineTo(W-pad, H-pad); ctx.moveTo(pad, H-pad); ctx.lineTo(pad, pad); ctx.stroke();
    // bars
    values.forEach((v,i)=>{
      const x = pad + 8 + i*(bw+8);
      const h = (H - pad*2) * (v/max);
      const y = H - pad - h;
      const grad = ctx.createLinearGradient(0,y,0,y+h);
      grad.addColorStop(0,'#16a34a'); grad.addColorStop(1,'#22c55e');
      ctx.fillStyle = grad; ctx.fillRect(x, y, bw, h);
    });
    // labels (trim)
    ctx.fillStyle = '#e5e7eb'; ctx.font = '12px system-ui'; ctx.textAlign = 'center';
    labels.forEach((t,i)=>{
      const x = pad + 8 + i*(bw+8) + bw/2; const y = H - pad + 12;
      const tt = t.length>8? t.slice(0,8)+'…': t; ctx.fillText(tt, x, y);
    });
  }

  const util = await reportesApi.utilidades(tipo, fecha);
  const utilEl = document.getElementById('util');
  utilEl.innerHTML = `
    <div class="metric-grid">
      <div class="metric"><h3>Total ventas</h3><div class="value">${util.totalVentas}</div></div>
      <div class="metric"><h3>Costo estimado</h3><div class="value">${util.costoEstimado}</div></div>
      <div class="metric"><h3>Utilidad</h3><div class="value">${util.utilidad}</div></div>
    </div>`;
}

document.getElementById('btnConsultar')?.addEventListener('click', consultar);
consultar();

document.getElementById('btnExcel')?.addEventListener('click', async ()=>{
  const tipo = document.getElementById('tipo').value; const fecha = document.getElementById('fecha').value;
  const res = await reportesApi.exportExcel(tipo, fecha); await downloadResponse(res, 'ventas.csv');
});
document.getElementById('btnPdf')?.addEventListener('click', async ()=>{
  const tipo = document.getElementById('tipo').value; const fecha = document.getElementById('fecha').value;
  const res = await reportesApi.exportPdf(tipo, fecha); await downloadResponse(res, 'reporte.pdf');
});

// Compras
function dateISO(d){ return d.toISOString().slice(0,10); }
const cIni = document.getElementById('cInicio'); const cFin = document.getElementById('cFin');
if (cIni && cFin) { const now = new Date(); cIni.value = dateISO(new Date(now.getFullYear(), now.getMonth(), 1)); cFin.value = dateISO(now); }

async function fillProveedores(){
  const sel = document.getElementById('cProveedor'); if (!sel) return;
  try { const list = await proveedoresApi.listar(); sel.innerHTML = '<option value="">Todos</option>' + list.map(p=>`<option value="${p.id}">${p.nombre}</option>`).join(''); } catch(_){ }
}
fillProveedores();

async function consultarCompras(){
  const inicio = document.getElementById('cInicio').value; const fin = document.getElementById('cFin').value;
  const idProveedor = document.getElementById('cProveedor').value || undefined;
  if (!inicio || !fin) return;
  const rpt = await reportesApi.compras(inicio, fin, idProveedor);
  const el = document.getElementById('resCompras');
  el.innerHTML = `<div class="metric-grid">
    <div class="metric"><h3>Periodo</h3><div class="value">${rpt.inicio}  ${rpt.fin}</div></div>
    <div class="metric"><h3>Total compras</h3><div class="value">${rpt.total}</div></div>
    <div class="metric"><h3>Transacciones</h3><div class="value">${rpt.transacciones}</div></div>
  </div>`;
}

document.getElementById('btnComprasConsultar')?.addEventListener('click', consultarCompras);
document.getElementById('btnComprasCsv')?.addEventListener('click', async ()=>{
  const inicio = document.getElementById('cInicio').value; const fin = document.getElementById('cFin').value;
  const idProveedor = document.getElementById('cProveedor').value || undefined;
  if (!inicio || !fin) return;
  const res = await reportesApi.comprasCsv(inicio, fin, idProveedor); await downloadResponse(res, 'compras.csv');
});
document.getElementById('btnComprasPdf')?.addEventListener('click', async ()=>{
  const inicio = document.getElementById('cInicio').value; const fin = document.getElementById('cFin').value;
  const idProveedor = document.getElementById('cProveedor').value || undefined;
  if (!inicio || !fin) return;
  const res = await reportesApi.comprasPdf(inicio, fin, idProveedor); await downloadResponse(res, 'compras.pdf');
});
