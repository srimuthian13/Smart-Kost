/* ============================================
   ADMIN DASHBOARD — Smart Kost
   ============================================ */

const API = 'http://localhost:8080';   // relative path — works same-origin
let allPayments = [];
let incomeChartInst = null;
let roomChartInst   = null;
let currentPayFilter = 'ALL';

// ---- INIT ----
document.addEventListener('DOMContentLoaded', () => {
  guardRole('ADMIN');
  initUser();
  initDate();
  loadAllStats();
  loadPendingPayments();
  initCharts();
});

// ---- AUTH GUARD ----
function guardRole(required) {
  const role = sessionStorage.getItem('role');
  if (role && role !== required) {
    window.location.href = '/login';
  }
}

// ---- USER INFO ----
function initUser() {
  const name = sessionStorage.getItem('username') || 'Admin';
  const letter = name.charAt(0).toUpperCase();
  document.getElementById('sidebarName').textContent = name;
  document.getElementById('sidebarAvatar').textContent = letter;
  document.getElementById('topAvatar').textContent = letter;
}

// ---- DATE ----
function initDate() {
  const now = new Date();
  const opts = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
  document.getElementById('pageDate').textContent = now.toLocaleDateString('id-ID', opts);
}

// ---- REFRESH ----
function refreshAll() {
  showToast('Data sedang diperbarui...', 'warning');
  loadAllStats();
  loadPendingPayments();
  if (document.getElementById('sec-payments').style.display !== 'none') {
    loadAllPayments();
  }
}

// ---- STATS ----
async function loadAllStats() {
  await Promise.allSettled([
    loadStat(`${API}/api/rooms/count`,              'statTotalRooms',     v => v),
    loadStat(`${API}/api/rooms/available/count`,    'statAvailRooms',     v => v),
    loadStat(`${API}/api/tenants/active/count`,     'statActiveTenants',  v => v),
    loadStat(`${API}/api/complaints/active/count`,  'statComplaints',     v => v),
  ]);
  loadPaymentSummary();
}

async function loadStat(url, elId, formatter, cb) {
  try {
    const res  = await fetchWithToken(url);
    const data = await res.json();
    // Extract numeric value if response is an object with a 'value' field
    const extracted = (typeof data === 'object' && data !== null && 'value' in data) ? data.value : data;
    const el   = document.getElementById(elId);
    if (el) el.textContent = formatter ? formatter(extracted) : extracted;
    if (cb) cb(data);
  } catch {
    const el = document.getElementById(elId);
    if (el) el.textContent = '—';
  }
}

function updatePendingBadge(count) {
  const badge    = document.getElementById('pendingBadge');
  const notifBadge = document.getElementById('notifBadge');
  const notifBtn   = document.getElementById('notifBtn');
  if (badge) badge.textContent = count;
  if (notifBadge) {
    notifBadge.textContent = count;
    notifBadge.style.display = count > 0 ? 'grid' : 'none';
  }
  if (notifBtn) notifBtn.style.color = count > 0 ? 'var(--danger)' : '';
}

// ---- CHARTS ----
function initCharts() {
  initIncomeChart();
  initRoomChart();
}

function initIncomeChart() {
  const ctx = document.getElementById('incomeChart')?.getContext('2d');
  if (!ctx) return;
  const months = ['Jan','Feb','Mar','Apr','Mei','Jun'];
  incomeChartInst = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: months,
      datasets: [{
        label: 'Pendapatan (Rp)',
        data: [0, 0, 0, 0, 0, 0],
        backgroundColor: 'rgba(139,74,19,.15)',
        borderColor: '#8b4a13',
        borderWidth: 2,
        borderRadius: 8,
        borderSkipped: false,
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: true,
      plugins: { legend: { display: false } },
      scales: {
        y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,.05)' },
          ticks: { callback: v => 'Rp' + fmt(v) } },
        x: { grid: { display: false } }
      }
    }
  });

  // Try to load real data
  fetchWithToken(`${API}/api/payments/monthly-revenue`)
    .then(r => r.json())
    .then(data => {
      if (Array.isArray(data) && data.length > 0) {
        incomeChartInst.data.datasets[0].data = data.map(d => d.revenue || 0);
        incomeChartInst.data.labels = data.map(d => 'Bulan ' + d.month);
        incomeChartInst.update();
      }
    }).catch(() => {});
}

async function loadPaymentSummary() {
  try {
    const res = await fetchWithToken(`${API}/api/payments/summary`);
    if (!res.ok) throw new Error('Gagal memuat ringkasan pembayaran');
    const data = await res.json();
    const overdueEl = document.getElementById('statOverdue');
    if (overdueEl) overdueEl.textContent = data.overdueInvoices ?? '—';
    
    const pendingEl = document.getElementById('statPending');
    if (pendingEl) pendingEl.textContent = data.pendingInvoices ?? '—';
    if (typeof updatePendingBadge === 'function') updatePendingBadge(data.pendingInvoices ?? 0);
    
    const incomeEl = document.getElementById('statIncome');
    if (incomeEl) incomeEl.textContent = data.totalRevenue !== undefined ? 'Rp ' + fmt(data.totalRevenue) : '—';
    
    renderReportSummary(data);
  } catch {
    const overdueEl = document.getElementById('statOverdue');
    if (overdueEl) overdueEl.textContent = '—';
    const pendingEl = document.getElementById('statPending');
    if (pendingEl) pendingEl.textContent = '—';
    const incomeEl = document.getElementById('statIncome');
    if (incomeEl) incomeEl.textContent = '—';
    
    const content = document.getElementById('reportContent');
    if (content) {
      content.innerHTML = `<div class="sk-empty"><div class="empty-icon">📊</div><p>Laporan keuangan detail belum tersedia. Coba lagi nanti.</p></div>`;
    }
  }
}

function renderReportSummary(summary) {
  const content = document.getElementById('reportContent');
  if (!content) return;
  content.innerHTML = `
    <div class="sk-report-cards">
      <div class="sk-report-card">
        <div class="sk-report-title">Total Invoice</div>
        <div class="sk-report-value">${summary.totalInvoices ?? 0}</div>
      </div>
      <div class="sk-report-card">
        <div class="sk-report-title">Lunas</div>
        <div class="sk-report-value">${summary.paidInvoices ?? 0}</div>
      </div>
      <div class="sk-report-card">
        <div class="sk-report-title">Pending</div>
        <div class="sk-report-value">${summary.pendingInvoices ?? 0}</div>
      </div>
      <div class="sk-report-card">
        <div class="sk-report-title">Tunggakan</div>
        <div class="sk-report-value">${summary.overdueInvoices ?? 0}</div>
      </div>
      <div class="sk-report-card">
        <div class="sk-report-title">Total Pendapatan</div>
        <div class="sk-report-value">Rp ${fmt(summary.totalRevenue ?? 0)}</div>
      </div>
    </div>
  `;
}

async function downloadPaymentsReport() {
  try {
    const token = sessionStorage.getItem('accessToken');
    const headers = { 'Authorization': `Bearer ${token}` };
    const res = await fetch(`${API}/api/payments/export`, { headers });
    if (!res.ok) throw new Error('Gagal mengunduh laporan');
    const blob = await res.blob();
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'payments-report.xlsx';
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (e) {
    showToast('Gagal mengunduh laporan. Periksa koneksi dan coba lagi.', 'error');
  }
}

function initRoomChart() {
  const ctx = document.getElementById('roomChart')?.getContext('2d');
  if (!ctx) return;
  roomChartInst = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Terisi', 'Kosong', 'Maintenance'],
      datasets: [{
        data: [0, 0, 0],
        backgroundColor: ['#8b4a13','#2d7a3a','#a16207'],
        borderWidth: 0,
        hoverOffset: 6,
      }]
    },
    options: {
      responsive: true, maintainAspectRatio: true,
      plugins: {
        legend: { position: 'bottom', labels: { padding: 16, usePointStyle: true } }
      },
      cutout: '62%',
    }
  });

  Promise.allSettled([
    fetchWithToken(`${API}/api/rooms/count`).then(r => r.json()),
    fetchWithToken(`${API}/api/rooms/available/count`).then(r => r.json()),
  ]).then(([total, avail]) => {
    const t = total.value || 0;
    const a = avail.value || 0;
    const o = t - a;
    roomChartInst.data.datasets[0].data = [o, a, 0];
    roomChartInst.update();
  }).catch(() => {});
}

// ---- PENDING PAYMENTS (quick view) ----
async function loadPendingPayments() {
  const tbody = document.getElementById('quickPaymentTable');
  if (!tbody) return;
  try {
    const res     = await fetchWithToken(`${API}/api/payments/status/WAITING_CONFIRMATION`);
    const pending = await res.json();
    if (!pending.length) {
      tbody.innerHTML = `<tr><td colspan="6"><div class="sk-empty"><div class="empty-icon">✅</div><p>Tidak ada pembayaran pending.</p></div></td></tr>`;
      return;
    }
    tbody.innerHTML = pending.slice(0, 5).map(p => `
      <tr>
        <td><div style="display:flex;align-items:center;gap:8px">
          <div class="avatar-circle" style="width:30px;height:30px;font-size:11px">${(p.tenantId || '?').toString().charAt(0)}</div>
          ID ${p.tenantId || '—'}
        </div></td>
        <td>Kamar ${p.roomId || '—'}</td>
        <td><strong>Rp ${fmt(p.amount || 0)}</strong></td>
        <td><span class="sk-badge sk-badge-info">${p.paymentMethod || '—'}</span></td>
        <td>${p.proofUrl ? `<img src="${p.proofUrl}" class="proof-img" onclick="viewProof('${p.proofUrl}')" alt="Bukti"/>` : '<span style="color:var(--text-muted);font-size:12px">Tidak ada</span>'}</td>
        <td style="display:flex;gap:6px">
          <button class="sk-btn sk-btn-success sk-btn-sm" onclick="approvePayment(${p.id},this)">
            <span class="material-symbols-outlined" style="font-size:14px">check</span> Setujui
          </button>
          <button class="sk-btn sk-btn-outline sk-btn-sm" onclick="rejectPayment(${p.id},this)">
            <span class="material-symbols-outlined" style="font-size:14px">close</span>
          </button>
        </td>
      </tr>
    `).join('');
  } catch {
    tbody.innerHTML = `<tr><td colspan="6"><div class="sk-empty"><div class="empty-icon">⚠️</div><p>Gagal memuat data pembayaran.</p></div></td></tr>`;
  }
}

// ---- ALL PAYMENTS ----
async function loadAllPayments() {
  const tbody = document.getElementById('allPaymentTable');
  if (!tbody) return;
  try {
    const res = await fetchWithToken(`${API}/api/payments`);
    allPayments = await res.json();
    renderPaymentTable(allPayments);
  } catch {
    tbody.innerHTML = `<tr><td colspan="9"><div class="sk-empty"><div class="empty-icon">⚠️</div><p>Gagal memuat data.</p></div></td></tr>`;
  }
}

function filterPayments(status, btn) {
  currentPayFilter = status;
  document.querySelectorAll('.filter-chip').forEach(c => c.classList.remove('active'));
  btn.classList.add('active');
  const filtered = status === 'ALL' ? allPayments : allPayments.filter(p => p.status === status);
  renderPaymentTable(filtered);
}

function renderPaymentTable(data) {
  const tbody = document.getElementById('allPaymentTable');
  if (!data.length) {
    tbody.innerHTML = `<tr><td colspan="9"><div class="sk-empty"><div class="empty-icon">📋</div><p>Tidak ada data pembayaran.</p></div></td></tr>`;
    return;
  }
  tbody.innerHTML = data.map((p, i) => `
    <tr>
      <td>${i + 1}</td>
      <td>ID ${p.tenantId || '—'}</td>
      <td>Kamar ${p.roomId || '—'}</td>
      <td><strong>Rp ${fmt(p.amount || 0)}</strong></td>
      <td><span class="sk-badge sk-badge-info">${p.paymentMethod || '—'}</span></td>
      <td>${formatDate(p.paymentDate)}</td>
      <td>${statusBadge(p.status)}</td>
      <td>${p.proofUrl ? `<img src="${p.proofUrl}" class="proof-img" onclick="viewProof('${p.proofUrl}')" alt="Bukti"/>` : '—'}</td>
      <td>${p.status === 'WAITING_CONFIRMATION' ? `
        <button class="sk-btn sk-btn-success sk-btn-sm" onclick="approvePayment(${p.id},this)">
          <span class="material-symbols-outlined" style="font-size:14px">check</span> Setujui
        </button>` : '<span style="color:var(--text-muted);font-size:12px">—</span>'}
      </td>
    </tr>
  `).join('');
}

// ---- APPROVE / REJECT ----
async function approvePayment(paymentId, btn) {
  btn.disabled = true;
  btn.innerHTML = '<span class="sk-loader"></span>';
  try {
    const res = await fetchWithToken(`${API}/api/payments/${paymentId}/confirm`, { method: 'POST' });
    if (!res.ok) throw new Error('Gagal');
    showToast('✅ Pembayaran berhasil disetujui! Invoice dikirim via email.', 'success');
    loadPendingPayments();
    loadAllStats();
    if (document.getElementById('sec-payments').style.display !== 'none') loadAllPayments();
  } catch {
    showToast('Gagal menyetujui pembayaran.', 'error');
    btn.disabled = false;
    btn.innerHTML = '<span class="material-symbols-outlined" style="font-size:14px">check</span> Setujui';
  }
}

async function rejectPayment(paymentId, btn) {
  if (!confirm('Tolak pembayaran ini?')) return;
  btn.disabled = true;
  try {
    await fetchWithToken(`${API}/api/payments/${paymentId}/cancel`, { method: 'PATCH' });
    showToast('Pembayaran ditolak.', 'warning');
    loadPendingPayments();
    loadAllStats();
  } catch {
    showToast('Gagal menolak pembayaran.', 'error');
    btn.disabled = false;
  }
}

// ---- SECTION NAVIGATION ----
function showSection(name) {
  const sections = ['dashboard','payments','complaints','rooms','tenants','reports'];
  sections.forEach(s => {
    const el = document.getElementById(`sec-${s}`);
    if (el) el.style.display = s === name ? 'block' : 'none';
  });

  const titles = {
    dashboard: 'Dashboard Admin',
    payments:  'Manajemen Pembayaran',
    complaints:'Manajemen Komplain',
    rooms:     'Manajemen Kamar',
    tenants:   'Data Penghuni',
    reports:   'Laporan Keuangan'
  };
  document.getElementById('pageTitle').textContent = titles[name] || 'Dashboard';

  // Update active nav
  document.querySelectorAll('.sk-sidebar-nav a').forEach(a => {
    if (a.getAttribute('onclick')?.includes(`'${name}'`) || (name === 'dashboard' && a.getAttribute('href')?.includes('/dashboard/admin'))) {
      a.classList.add('active');
    } else {
      a.classList.remove('active');
    }
  });

  if (name === 'payments') loadAllPayments();
  if (name === 'complaints') loadAllComplaints();
  if (name === 'tenants') loadAllTenants();
  if (name === 'rooms') loadAdminRooms();

  // Close sidebar on mobile
  if (window.innerWidth <= 768) toggleSidebar(false);
}

// ---- SIDEBAR TOGGLE ----
function toggleSidebar(force) {
  const sb = document.getElementById('sidebar');
  const isOpen = sb.classList.contains('open');
  sb.classList.toggle('open', force !== undefined ? force : !isOpen);
}

// ---- MODAL ----
function viewProof(url) {
  document.getElementById('modalImg').src = url;
  document.getElementById('imgModal').classList.add('open');
}
function closeModal(id) {
  document.getElementById(id).classList.remove('open');
}

// ---- LOGOUT ----
function doLogout() {
  sessionStorage.clear();
  localStorage.removeItem('rememberedEmail');
  window.location.href = '/login';
}

// ---- HELPERS ----
function fmt(n) {
  return new Intl.NumberFormat('id-ID').format(n);
}

function formatDate(d) {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('id-ID', { day:'2-digit', month:'short', year:'numeric' });
}

function statusBadge(s) {
  const map = {
    PENDING:  '<span class="sk-badge sk-badge-warning">⏳ Belum Bayar</span>',
    WAITING_CONFIRMATION: '<span class="sk-badge sk-badge-warning">⏳ Verifikasi</span>',
    PAID: '<span class="sk-badge sk-badge-success">✅ Lunas</span>',
    OVERDUE: '<span class="sk-badge sk-badge-danger">❗ Telat</span>',
    CANCELLED: '<span class="sk-badge sk-badge-danger">❌ Batal</span>',
  };
  return map[s] || `<span class="sk-badge sk-badge-brown">${s}</span>`;
}

async function fetchWithToken(url, options = {}) {
  const token = sessionStorage.getItem('accessToken');
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return fetch(url, { ...options, headers });
}

function showToast(msg, type = 'success') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `sk-toast ${type}`;
  toast.innerHTML = `<span class="material-symbols-outlined" style="font-size:18px">${type === 'success' ? 'check_circle' : type === 'error' ? 'error' : 'warning'}</span> ${msg}`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

// ---- ADMIN COMPLAINTS LOGIC ----
async function loadAllComplaints() {
  const tbody = document.getElementById('allComplaintsTable');
  if (!tbody) return;
  try {
    const res = await fetchWithToken(`${API}/api/complaints`);
    const complaints = await res.json();
    if (!complaints || !complaints.length) {
      tbody.innerHTML = `<tr><td colspan="7"><div class="sk-empty"><div class="empty-icon"><i class="fa-solid fa-circle-check"></i></div><p>Tidak ada keluhan saat ini.</p></div></td></tr>`;
      return;
    }
    tbody.innerHTML = complaints.map(c => {
      let badge = '';
      if (c.status === 'PENDING') badge = '<span class="sk-badge sk-badge-warning">PENDING</span>';
      else if (c.status === 'PROCESS') badge = '<span class="sk-badge sk-badge-info">PROSES</span>';
      else if (c.status === 'DONE') badge = '<span class="sk-badge sk-badge-success">SELESAI</span>';
      else badge = '<span class="sk-badge sk-badge-danger">DITOLAK</span>';

      let actions = '';
      if (c.status === 'PENDING') {
        actions = `
          <button class="sk-btn sk-btn-primary sk-btn-sm" onclick="updateComplaintStatus(${c.id}, 'PROCESS')">Proses</button>
          <button class="sk-btn sk-btn-danger sk-btn-sm" onclick="updateComplaintStatus(${c.id}, 'REJECTED')">Tolak</button>
        `;
      } else if (c.status === 'PROCESS') {
        actions = `
          <button class="sk-btn sk-btn-success sk-btn-sm" onclick="updateComplaintStatus(${c.id}, 'DONE')">Selesai</button>
        `;
      } else {
        actions = '<span style="color:var(--text-muted);font-size:12px">Selesai</span>';
      }

      return `
        <tr>
          <td>COMP-${c.id}</td>
          <td>Tenant ${c.tenantId || '—'}</td>
          <td>Kamar ${c.roomId || '—'}</td>
          <td><strong>${c.title || '—'}</strong></td>
          <td>${c.description || '—'}</td>
          <td>${badge}</td>
          <td><div style="display:flex;gap:6px">${actions}</div></td>
        </tr>
      `;
    }).join('');
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px;">Gagal memuat komplain.</td></tr>`;
  }
}

async function updateComplaintStatus(id, status) {
  try {
    const res = await fetchWithToken(`${API}/api/complaints/${id}/status?status=${status}`, { method: 'PUT' });
    if (!res.ok) throw new Error('Gagal');
    showToast(`Status komplain berhasil diperbarui!`, 'success');
    loadAllComplaints();
    loadAllStats();
  } catch {
    showToast('Gagal memperbarui status komplain.', 'error');
  }
}

// ---- ADMIN TENANTS LOGIC ----
async function loadAllTenants() {
  const tbody = document.getElementById('allTenantsTable');
  if (!tbody) return;
  try {
    const res = await fetchWithToken(`${API}/api/tenants`);
    const tenants = await res.json();
    if (!tenants || !tenants.length) {
      tbody.innerHTML = `<tr><td colspan="8"><div class="sk-empty"><div class="empty-icon"><i class="fa-solid fa-users"></i></div><p>Tidak ada penghuni aktif.</p></div></td></tr>`;
      return;
    }
    tbody.innerHTML = tenants.map(t => {
      let isCheckout = t.checkout;
      let action = isCheckout 
        ? '<span style="color:var(--text-muted);font-size:12px">Checked Out</span>'
        : `<button class="sk-btn sk-btn-danger sk-btn-sm" onclick="checkoutTenant(${t.id})">Checkout</button>
           <button class="sk-btn sk-btn-outline sk-btn-sm" onclick="blacklistTenant(${t.id})" style="border-color:var(--danger);color:var(--danger)">Blacklist</button>`;

      return `
        <tr>
          <td>TEN-${t.id}</td>
          <td><strong>${t.name || '—'}</strong></td>
          <td>Kamar ${t.roomId || '—'}</td>
          <td>${t.phone || '—'}</td>
          <td>${t.email || '—'}</td>
          <td>${formatDate(t.startDate)}</td>
          <td>${formatDate(t.endDate)}</td>
          <td>${action}</td>
        </tr>
      `;
    }).join('');
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; padding:20px;">Gagal memuat data penghuni.</td></tr>`;
  }
}

async function checkoutTenant(id) {
  if (!confirm('Yakin ingin melakukan checkout untuk penghuni ini?')) return;
  try {
    const res = await fetchWithToken(`${API}/api/tenants/${id}/checkout`, { method: 'POST' });
    if (!res.ok) throw new Error('Gagal');
    showToast('Penghuni berhasil di-checkout!', 'success');
    loadAllTenants();
    loadAllStats();
  } catch (e) {
    showToast('Gagal melakukan checkout penghuni.', 'error');
  }
}

async function blacklistTenant(id) {
  if (!confirm('Blacklist penghuni ini?')) return;
  try {
    const res = await fetchWithToken(`${API}/api/tenants/blacklist?tenantId=${id}`, { method: 'POST' });
    if (!res.ok) throw new Error('Gagal');
    showToast('Penghuni berhasil di-blacklist.', 'warning');
    loadAllTenants();
    loadAllStats();
  } catch {
    showToast('Gagal blacklist penghuni.', 'error');
  }
}

// ---- ADMIN ROOMS DISPLAY ----
async function loadAdminRooms() {
  const container = document.getElementById('adminRoomsContainer');
  if (!container) return;
  try {
    const res = await fetchWithToken(`${API}/api/rooms?size=100`);
    const data = await res.json();
    const rooms = data.content || data || [];

    if (!rooms.length) {
      container.innerHTML = `<div class="sk-empty"><div class="empty-icon"><i class="fa-solid fa-bed"></i></div><p>Belum ada data kamar.</p></div>`;
      return;
    }

    container.innerHTML = rooms.map(room => {
      const isAvail = room.status === 'AVAILABLE';
      const badgeClass = isAvail ? 'sk-badge-success' : (room.status === 'OCCUPIED' ? 'sk-badge-primary' : 'sk-badge-warning');
      const statusText = isAvail ? 'Tersedia' : (room.status === 'OCCUPIED' ? 'Terisi' : 'Maintenance');
      
      const imgUrl = (room.images && room.images.length > 0) ? room.images[0] : 'https://via.placeholder.com/150?text=No+Image';

      return `
        <div style="display:flex; gap:16px; align-items:center; padding:12px; border:1px solid rgba(201,166,107,0.15); border-radius:8px; margin-bottom:12px; background:rgba(201,166,107,0.03);">
          <img src="${imgUrl}" alt="Kamar ${room.roomNumber}" style="width:80px; height:80px; object-fit:cover; border-radius:6px;" onerror="this.onerror=null;this.src='https://via.placeholder.com/150?text=No+Image';"/>
          <div style="flex:1;">
            <div style="font-weight:700; font-size:16px; margin-bottom:4px; color:var(--text-primary);">Kamar ${room.roomNumber} <span style="font-size:13px; font-weight:400; color:var(--text-muted);">(${room.roomType})</span></div>
            <div style="font-size:14px; margin-bottom:6px;">Rp ${fmt(room.price)} / bln — Kapasitas: ${room.capacity} org</div>
            <span class="sk-badge ${badgeClass}">${statusText}</span>
          </div>
          <div>
            <a href="/admin/rooms?edit=${room.id}" class="sk-btn sk-btn-outline sk-btn-sm" style="text-decoration:none;"><span class="material-symbols-outlined" style="font-size:16px;">edit</span> Edit</a>
          </div>
        </div>
      `;
    }).join('');
  } catch (e) {
    container.innerHTML = `<div class="sk-empty"><div class="empty-icon"><i class="fa-solid fa-triangle-exclamation"></i></div><p>Gagal memuat data kamar.</p></div>`;
  }
}