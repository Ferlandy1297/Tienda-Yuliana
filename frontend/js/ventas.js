import { requireAuth, setUserUI, logout } from './auth.js';
import { ventasApi, pagosVentaApi, productosApi, clientesApi, fiadosApi } from './api.js';

requireAuth(); setUserUI();
document.getElementById('btnLogout')?.addEventListener('click', logout);

const items = document.getElementById('items');
const tpl = document.getElementById('tplItem');
const feedback = document.getElementById('feedback');
const barcodeInput = document.getElementById('barcode');
const addByBarcodeBtn = document.getElementById('addByBarcode');
const searchInput = document.getElementById('searchName');
const searchResults = document.getElementById('searchResults');

let productosCache = [];
let mapByCodigo = new Map();
let mapById = new Map();
async function loadProductosCache(){
  try {
    productosCache = await productosApi.listar();
    mapByCodigo = new Map(productosCache.map(p => [String(p.codigoBarras||'').trim(), p]));
    mapById = new Map(productosCache.map(p => [String(p.id), p]));
  } catch (e) { console.warn('No se pudo cargar productos', e); }
}

function addItem(){
  const node = tpl.content.cloneNode(true);
  node.querySelector('.btn-del').addEventListener('click', (e)=>{
    e.currentTarget.closest('.row').remove();
    computeTotal();
  });
  items.appendChild(node);
  const row = items.lastElementChild;
  const qty = row.querySelector('.cantidad'); qty?.addEventListener('input', computeTotal);
  const idInput = row.querySelector('.idProducto');
  idInput?.addEventListener('change', ()=>{
    const prod = mapById.get(String(idInput.value));
    setRowProductInfo(row, prod);
  });
  // fill products select
  fillProductosSelect(idInput);
  return row;
}
document.getElementById('addItem')?.addEventListener('click', addItem);
if (items && !items.children.length) addItem();

function setRowProductInfo(row, prod){
  const idInput = row.querySelector('.idProducto');
  if (idInput && prod?.id) idInput.value = String(prod.id);
  const nameEl = row.querySelector('[data-product-name]');
  if (nameEl) nameEl.textContent = prod ? `${prod.nombre} (${prod.codigoBarras||''})` : '';
  computeTotal();
}

function findRowByProductId(pid){
  return Array.from(items.querySelectorAll('.row')).find(r => String(r.querySelector('.idProducto')?.value||'') === String(pid));
}

async function addByBarcode(code){
  const c = String(code||'').trim(); if (!c) return;
  let prod = mapByCodigo.get(c);
  if (!prod) { await loadProductosCache(); prod = mapByCodigo.get(c); }
  if (!prod) { feedback.textContent = `No encontrado: ${c}`; feedback.className = 'error'; return; }
  // si existe fila, incrementa cantidad
  const existing = findRowByProductId(prod.id);
  if (existing) {
    const qty = existing.querySelector('.cantidad');
    qty.value = Number(qty.value||'0') + 1;
  } else {
    const row = addItem();
    setRowProductInfo(row, prod);
  }
  if (barcodeInput) { barcodeInput.value = ''; barcodeInput.focus(); }
  computeTotal();
}

addByBarcodeBtn?.addEventListener('click', () => addByBarcode(barcodeInput?.value));
barcodeInput?.addEventListener('keydown', (e)=>{ if (e.key === 'Enter') { e.preventDefault(); addByBarcode(barcodeInput.value); } });

function renderResults(list){
  if (!searchResults) return;
  searchResults.innerHTML = '';
  if (!list.length) {
    const li = document.createElement('li'); li.textContent = 'Sin resultados'; li.className='tag';
    searchResults.appendChild(li); return;
  }
  list.slice(0, 8).forEach(p => {
    const li = document.createElement('li');
    li.className = 'hstack';
    li.innerHTML = `<span>${p.nombre}</span> <span class="tag right">${p.codigoBarras||''}</span>`;
    li.addEventListener('click', ()=>{
      // si existe fila, incrementa cantidad; sino crea fila
      const existing = findRowByProductId(p.id);
      if (existing) {
        const qty = existing.querySelector('.cantidad'); qty.value = Number(qty.value||'0') + 1;
      } else {
        const row = addItem(); setRowProductInfo(row, p);
      }
      computeTotal();
    });
    searchResults.appendChild(li);
  });
}

async function handleSearchInput(){
  const q = String(searchInput.value||'').trim().toLowerCase();
  if (!q) { searchResults.innerHTML=''; return; }
  if (!productosCache.length) await loadProductosCache();
  const filtered = productosCache.filter(p =>
    (p.nombre||'').toLowerCase().includes(q) || String(p.codigoBarras||'').toLowerCase().includes(q)
  );
  renderResults(filtered);
}

searchInput?.addEventListener('input', handleSearchInput);

function computeTotal(){
  const totalEl = document.getElementById('totalEst');
  if (!totalEl) return;
  let total = 0;
  const rows = Array.from(items.querySelectorAll('.row'));
  rows.forEach(r => {
    const id = r.querySelector('.idProducto')?.value;
    const qty = Number(r.querySelector('.cantidad')?.value || '0');
    if (!id || !qty) return;
    const prod = mapById.get(String(id));
    if (!prod) return;
    const price = Number(prod.precioUnitario);
    if (!isNaN(price)) total += price * qty;
  });
  totalEl.textContent = total.toFixed(2);
  computeCambio(total);
}

function computeCambio(totalVal){
  const total = typeof totalVal === 'number' ? totalVal : Number(document.getElementById('totalEst')?.textContent || '0');
  const metodo = document.getElementById('metodo')?.value;
  const monto = Number(document.getElementById('monto')?.value || '0');
  const cambioEl = document.getElementById('cambioEst');
  if (!cambioEl) return;
  if (metodo === 'EFECTIVO') {
    const cambio = Math.max(monto - total, 0);
    cambioEl.textContent = cambio.toFixed(2);
  } else {
    cambioEl.textContent = '0.00';
  }
}

document.getElementById('monto')?.addEventListener('input', () => computeCambio());
document.getElementById('metodo')?.addEventListener('change', () => computeCambio());

document.getElementById('btnVender')?.addEventListener('click', async ()=>{
  feedback.textContent=''; feedback.className='';
  const tipo = document.getElementById('tipo').value;
  const idClienteRaw = document.getElementById('idCliente').value;
  const idCliente = idClienteRaw ? Number(idClienteRaw) : undefined;
  const metodo = document.getElementById('metodo').value;
  const monto = Number(document.getElementById('monto').value || '0');
  const itemsData = Array.from(items.querySelectorAll('.row')).map(r => ({
    idProducto: Number(r.querySelector('.idProducto').value),
    cantidad: Number(r.querySelector('.cantidad').value)
  })).filter(x => x.idProducto && x.cantidad);
  if (!itemsData.length) { feedback.textContent='Agrega al menos un ítem'; feedback.className='error'; return; }
  // Evita FIADO si cliente bloqueado
  if (tipo === 'FIADO' && idCliente) {
    try {
      const clientes = await fiadosApi.clientes();
      const cli = clientes.find(c => String(c.idCliente) === String(idCliente));
      if (cli?.bloqueado) { feedback.textContent = 'Cliente bloqueado por morosidad'; feedback.className='error'; return; }
    } catch (_) {}
  }
  const req = { tipo, items: itemsData, pago: { metodo, monto: String(monto) } };
  if (idCliente) req.idCliente = idCliente;
  try {
    const res = await ventasApi.registrar(req);
    feedback.innerHTML = `<div class="success">Venta #${res.idVenta} registrada. Total: ${res.total}. Cambio: ${res.cambio ?? 0}</div>`;
    items.innerHTML=''; addItem();
    document.getElementById('monto').value = '0';
    loadHist();
    // Render ticket
    try {
      const t = await ventasApi.ticket(res.idVenta);
      document.getElementById('tIdVenta').value = String(res.idVenta);
      renderTicket(t);
    } catch(_){ }
  } catch (e) { feedback.textContent = e.message; feedback.className='error'; }
});

const tbody = document.querySelector('#tblVentas tbody');
async function loadHist(){
  if (!tbody) return;
  const i = document.getElementById('inicio').value; const f = document.getElementById('fin').value;
  const list = await ventasApi.listar(i||undefined, f||undefined);
  tbody.innerHTML='';
  list.forEach(v => {
    const tr = document.createElement('tr');
    if (v.id != null) tr.setAttribute('data-id', String(v.id));
    tr.innerHTML = `<td>${v.fechaHora}</td><td>${v.tipo}</td><td>${v.estado}</td><td>${v.total}</td>`;
    tbody.appendChild(tr);
  });
  fillVentasSelect(list);
}
document.getElementById('btnBuscar')?.addEventListener('click', loadHist);
loadHist();

document.getElementById('btnPagarVenta')?.addEventListener('click', async ()=>{
  const pvFeedback = document.getElementById('pvFeedback'); if (!pvFeedback) return;
  pvFeedback.textContent=''; pvFeedback.className='';
  const idVenta = Number(document.getElementById('pvIdVenta').value);
  if (!idVenta) { pvFeedback.textContent='Selecciona una venta'; pvFeedback.className='error'; return; }
  const metodo = document.getElementById('pvMetodo').value;
  const monto = String(Number(document.getElementById('pvMonto').value||'0'));
  try {
    const v = await pagosVentaApi.pagar({ idVenta, metodo, monto });
    pvFeedback.textContent = `Pago registrado. Estado venta: ${v.estado}`; pvFeedback.className='success';
    loadHist();
  } catch (e) { pvFeedback.textContent = e.message; pvFeedback.className='error'; }
});

async function fillClientesSelect(){
  const sel = document.getElementById('idCliente'); if (!sel) return;
  try { const cs = await clientesApi.listar(); sel.innerHTML = '<option value="">Sin cliente</option>' + cs.map(c=>`<option value="${c.id}">${c.nombre}</option>`).join(''); } catch(_){ }
}

async function fillProductosSelect(selectEl){
  try { const prods = productosCache.length ? productosCache : await productosApi.listar();
    selectEl.innerHTML = '<option value="">Selecciona producto</option>' + prods.map(p=>`<option value="${p.id}">${p.nombre} (${p.codigoBarras||''})</option>`).join('');
  } catch(_){ }
}

function fillVentasSelect(list){
  const sel = document.getElementById('pvIdVenta'); if (!sel) return;
  sel.innerHTML = '<option value="">Selecciona venta</option>' + (list||[]).map(v=>`<option value="${v.id}">#${v.id} • ${v.fechaHora} • ${v.total}</option>`).join('');
}

(async function init(){
  await loadProductosCache();
  await fillClientesSelect();
  // fill first row product options
  const firstSel = items?.querySelector('.idProducto'); if (firstSel) await fillProductosSelect(firstSel);
})();

// Ticket UI
function renderTicket(t) {
  const el = document.getElementById('ticketView'); if (!el) return;
  if (!t) { el.innerHTML = ''; return; }
  const itemsHtml = (t.items||[]).map(i => `<div class="hstack"><span>${i.producto} x${i.cantidad}</span><span class="right">${i.subtotal}</span></div>`).join('');
  el.innerHTML = `
    <div style="text-align:center"><strong>Tienda Yuliana</strong><br/><small>Ticket #${t.idVenta}</small><br/><small>${t.fechaHora||''}</small></div>
    <hr/>
    <div><small>Cliente:</small> ${t.cliente||'MOSTRADOR'}</div>
    <div><small>Atendió:</small> ${t.atendio||''}</div>
    <hr/>
    ${itemsHtml}
    <hr/>
    <div class="hstack"><strong>Total</strong><strong class="right">${t.total}</strong></div>
    ${t.metodoPago? `<div class="hstack"><span>${t.metodoPago}</span><span class="right">${t.montoPagado||''}</span></div>`:''}
    ${t.cambio? `<div class="hstack"><span>Cambio</span><span class="right">${t.cambio}</span></div>`:''}
  `;
}

document.getElementById('btnVerTicket')?.addEventListener('click', async ()=>{
  const id = Number(document.getElementById('tIdVenta').value||'0'); if (!id) return;
  try { const t = await ventasApi.ticket(id); renderTicket(t); } catch (e) { alert(e.message); }
});

document.getElementById('btnImprimirTicket')?.addEventListener('click', ()=>{
  window.print();
});
