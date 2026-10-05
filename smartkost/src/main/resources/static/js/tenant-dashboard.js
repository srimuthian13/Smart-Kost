/* ============================================
   TENANT DASHBOARD — Smart Kost
   ============================================ */

const API = 'http://localhost:8080';
let userId = null;
let myRoomId = null;

document.addEventListener('DOMContentLoaded', () => {
  guardRole('TENANT');
  initUser();
  initDate();
  loadDashboard().then(() => {
    initComplaintForm();
  });
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
  const name = sessionStorage.getItem('username') || 'Tenant';
  userId = sessionStorage.getItem('userId');
  const letter = name.charAt(0).toUpperCase();
  document.getElementById('welcomeName').textContent = `Halo, ${name}! 👋`;
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
  showToast('Memperbarui data...', 'warning');
  loadDashboard();
  if (document.getElementById('sec-invoices').style.display !== 'none') {
    loadInvoices();
  }
}

// ---- GLOBAL CACHE ----
let myTenant = null;

// ---- LOAD DASHBOARD ----
async function loadDashboard() {
  if (!userId) {
    document.getElementById('billStatusText').textContent = 'Sesi tidak valid (userId kosong). Silakan login ulang.';
    return;
  }
  try {
    // Fetch Tenant data first
    let localTenantId = null;
    try {
      const tenantRes = await fetchWithToken(`${API}/api/tenants/user/${userId}`);
      if (tenantRes.ok) {
        const tenants = await tenantRes.json();
        if (tenants && tenants.length > 0) {
          myTenant = tenants[tenants.length - 1];
          localTenantId = myTenant.id;
        }
      }
    } catch (err) {
      console.warn("Failed to load tenant profile info", err);
    }

    if (!localTenantId) {
      document.getElementById('billStatusText').textContent = 'Anda belum memiliki kamar sewa aktif.';
      return;
    }

    const res = await fetchWithToken(`${API}/api/payments/tenant/${localTenantId}`);
    if (!res.ok) {
      document.getElementById('billStatusText').textContent = `Gagal mengambil data dari server (Error ${res.status}).`;
      return;
    }
    const invoices = await res.json();
    
    if (Array.isArray(invoices) && invoices.length > 0) {
      // Find latest invoice (for billing status)
      const latest = invoices[invoices.length - 1];
      
      const approvedInvoices = invoices.filter(inv => inv.status === 'PAID');
      const activeInvoice = approvedInvoices.length > 0 ? approvedInvoices[approvedInvoices.length - 1] : latest;
      
      myRoomId = activeInvoice.roomId;
      document.getElementById('myRoomNum').textContent = activeInvoice.roomId || '—';
      
      const amtEl = document.getElementById('billAmount');
      const stEl  = document.getElementById('billStatusText');
      const btn   = document.getElementById('btnPayNow');
      const card  = document.getElementById('billCardWrap');
      const icon  = document.getElementById('billIcon');

      amtEl.textContent = `Rp ${fmt(latest.amount || 0)}`;

      const pStatus = latest.status || 'UNPAID';

      if (pStatus === 'PAID') {
        stEl.textContent = 'Tagihan bulan ini sudah lunas. Terima kasih!';
        btn.style.display = 'none';
        card.classList.add('paid');
        icon.innerHTML = '<span class="material-symbols-outlined">check_circle</span>';
      } else if (pStatus === 'WAITING_CONFIRMATION') {
        stEl.textContent = 'Pembayaran Anda sedang menunggu verifikasi admin.';
        btn.style.display = 'none';
        card.classList.remove('paid');
        icon.innerHTML = '<span class="material-symbols-outlined">hourglass_empty</span>';
      } else if (pStatus === 'PENDING') {
        stEl.textContent = 'Harap segera lunasi tagihan Anda.';
        btn.style.display = 'flex';
        btn.href = `/dashboard/tenant/payment?invoiceId=${latest.id}`;
        card.classList.remove('paid');
        icon.innerHTML = '<span class="material-symbols-outlined">receipt_long</span>';
      } else {
        // UNPAID / REJECTED / CANCELLED
        stEl.textContent = pStatus === 'CANCELLED' ? 'Pembayaran dibatalkan. Silakan hubungi admin.' : 'Harap segera lunasi tagihan Anda.';
        btn.style.display = 'flex';
        btn.href = `/dashboard/tenant/payment?invoiceId=${latest.id}`;
        card.classList.remove('paid');
        icon.innerHTML = '<span class="material-symbols-outlined">receipt_long</span>';
      }

      // Fill Activity
      const actList = document.getElementById('activityList');
      const actEmp  = document.getElementById('activityEmpty');
      actList.innerHTML = invoices.slice(-3).reverse().map(inv => `
        <li style="padding:12px 0;border-bottom:1px solid var(--border-light);display:flex;justify-content:space-between;align-items:center">
          <div>
            <div style="font-size:13px;font-weight:600;color:var(--text-primary)">Tagihan ${formatDateMonth(inv.dueDate)}</div>
            <div style="font-size:12px;color:var(--text-muted)">Kamar ${inv.roomId}</div>
          </div>
          ${statusBadge(inv.status || 'UNPAID')}
        </li>
      `).join('');
      actList.style.display = 'block';
      actEmp.style.display = 'none';

      // Load Room Details into Overview
      if (myRoomId) {
        try {
          const roomRes = await fetchWithToken(`${API}/api/rooms/${myRoomId}`);
          if (roomRes.ok) {
            const roomData = await roomRes.json();
            document.getElementById('overviewRoomDetailBody').innerHTML = renderRoomDetail(roomData, true);
            document.getElementById('overviewRoomDetailsCard').style.display = 'block';
          }
        } catch (err) {
          console.error("Gagal memuat kamar untuk overview:", err);
        }
      }

    } else {
      document.getElementById('billAmount').textContent = 'Rp 0';
      document.getElementById('billStatusText').textContent = 'Belum ada tagihan terdaftar untuk Anda.';
      document.getElementById('btnPayNow').style.display = 'none';
      document.getElementById('myRoomNum').textContent = '—';
      const navComplaint = document.querySelector('a[onclick="showSection(\\\'complaints\\\')"]');
      if (navComplaint) navComplaint.parentElement.style.display = 'none';
    }

  } catch (e) {
    document.getElementById('billStatusText').textContent = 'Terjadi kesalahan sistem: ' + e.message;
  }
}

// ---- INVOICES ----
async function loadInvoices() {
  if (!userId) return;
  const tbody = document.getElementById('invoiceTable');
  try {
    let localTenantId = myTenant ? myTenant.id : null;
    if (!localTenantId) {
      const tRes = await fetchWithToken(`${API}/api/tenants/user/${userId}`);
      if (tRes.ok) {
        const tenants = await tRes.json();
        if (tenants && tenants.length > 0) localTenantId = tenants[tenants.length - 1].id;
      }
    }
    if (!localTenantId) {
      tbody.innerHTML = `<tr><td colspan="5"><div class="sk-empty"><div class="empty-icon">📄</div><p>Tidak ada riwayat invoice.</p></div></td></tr>`;
      return;
    }
    const res = await fetchWithToken(`${API}/api/payments/tenant/${localTenantId}`);
    const invoices = await res.json();
    if (!invoices.length) {
      tbody.innerHTML = `<tr><td colspan="5"><div class="sk-empty"><div class="empty-icon">📄</div><p>Tidak ada riwayat invoice.</p></div></td></tr>`;
      return;
    }
    tbody.innerHTML = invoices.map((inv, i) => {
      const pStatus = inv.status || 'UNPAID';
      const actionBtn = (pStatus === 'UNPAID' || pStatus === 'PENDING' || pStatus === 'CANCELLED') 
        ? `<a href="/dashboard/tenant/payment?invoiceId=${inv.id}" class="sk-btn sk-btn-primary sk-btn-sm">Bayar</a>`
        : `<button class="sk-btn sk-btn-outline sk-btn-sm" disabled>Selesai</button>`;

      return `
        <tr>
          <td>INV-${inv.id}</td>
          <td>${formatDateMonth(inv.dueDate)}</td>
          <td><strong>Rp ${fmt(inv.amount || 0)}</strong></td>
          <td>${statusBadge(pStatus)}</td>
          <td>${actionBtn}</td>
        </tr>
      `;
    }).join('');
  } catch {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center">Gagal memuat</td></tr>`;
  }
}

// ---- NAVIGATION ----
function showSection(name) {
  const sections = ['overview','kamar','invoices','complaints'];
  sections.forEach(s => {
    const el = document.getElementById(`sec-${s}`);
    if (el) el.style.display = s === name ? 'block' : 'none';
  });
  
  const titles = { overview: 'Beranda Penghuni', kamar: 'Detail Kamar', invoices: 'Riwayat Invoice', complaints: 'Komplain' };
  document.getElementById('pageTitle').textContent = titles[name] || 'Beranda';
  
  document.querySelectorAll('.sk-sidebar-nav a').forEach(a => {
    if (a.getAttribute('onclick')?.includes(`'${name}'`)) {
      a.classList.add('active');
    } else {
      a.classList.remove('active');
    }
  });

  if (name === 'invoices') loadInvoices();
  if (name === 'kamar') loadKamar();
  if (name === 'complaints') loadComplaints();
  if (window.innerWidth <= 768) toggleSidebar(false);
}

function toggleSidebar(force) {
  const sb = document.getElementById('sidebar');
  sb.classList.toggle('open', force !== undefined ? force : !sb.classList.contains('open'));
}

// ---- LOAD KAMAR ----
async function loadKamar() {
  const detailBody = document.getElementById('kamarDetailBody');
  const listBody   = document.getElementById('kamarListBody');

  // Show my room detail from invoices (roomId stored from dashboard)
  try {
    if (!myTenant) throw new Error("Belum terdaftar sebagai tenant");
    const res = await fetchWithToken(`${API}/api/payments/tenant/${myTenant.id}`);
    if (!res.ok) throw new Error(`API Error ${res.status}`);
    const invoices = await res.json();

    if (Array.isArray(invoices) && invoices.length > 0) {
      const approvedInvoices = invoices.filter(inv => inv.payment && inv.payment.status === 'APPROVED');
      const activeInvoice = approvedInvoices.length > 0 ? approvedInvoices[approvedInvoices.length - 1] : invoices[invoices.length - 1];
      const roomId = activeInvoice.roomId;

      if (roomId) {
        const roomRes = await fetchWithToken(`${API}/api/rooms/${roomId}`);
        if (roomRes.ok) {
          const room = await roomRes.json();
          detailBody.innerHTML = renderRoomDetail(room, true);
        } else {
          detailBody.innerHTML = `<div class="sk-empty"><div class="empty-icon">🏠</div><p>Detail kamar tidak ditemukan (Error ${roomRes.status}).</p></div>`;
        }
      } else {
        detailBody.innerHTML = `<div class="sk-empty"><div class="empty-icon">🏠</div><p>Anda belum memiliki kamar terdaftar.</p></div>`;
      }
    } else {
      detailBody.innerHTML = `<div class="sk-empty"><div class="empty-icon">🏠</div><p>Anda belum memiliki kamar terdaftar.</p></div>`;
    }
  } catch (e) {
    detailBody.innerHTML = `<div class="sk-empty"><div class="empty-icon">⚠️</div><p>Gagal memuat detail kamar: ${e.message}</p></div>`;
  }
}

function renderRoomDetail(room, isMyRoom) {
  const facilityBadges = (room.facilities || []).map(f =>
    `<span style="background:var(--primary-light,#e8f0fe);color:var(--primary);padding:4px 10px;border-radius:20px;font-size:12px;font-weight:600;">${f}</span>`
  ).join('');

  return `
    <div style="display:flex;flex-direction:column;gap:16px;">
      ${isMyRoom ? '<div style="background:linear-gradient(135deg,var(--primary),#3b82f6);color:#fff;padding:8px 14px;border-radius:8px;font-size:12px;font-weight:700;width:fit-content;">KAMAR ANDA</div>' : ''}
      <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:12px;">
        <div style="background:var(--bg-card,#f8fafc);border-radius:10px;padding:14px;">
          <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Nomor Kamar</div>
          <div style="font-size:18px;font-weight:800;color:var(--text-primary)">${room.roomNumber || '—'}</div>
        </div>
        <div style="background:var(--bg-card,#f8fafc);border-radius:10px;padding:14px;">
          <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Tipe</div>
          <div style="font-size:16px;font-weight:700;color:var(--text-primary)">${room.roomType || '—'}</div>
        </div>
        <div style="background:var(--bg-card,#f8fafc);border-radius:10px;padding:14px;">
          <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Kapasitas</div>
          <div style="font-size:16px;font-weight:700;color:var(--text-primary)">${room.capacity || '—'} orang</div>
        </div>
        <div style="background:var(--bg-card,#f8fafc);border-radius:10px;padding:14px;">
          <div style="font-size:11px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Harga</div>
          <div style="font-size:16px;font-weight:700;color:var(--success)">Rp ${fmt(room.price || 0)}<span style="font-size:11px;color:var(--text-muted)">/bln</span></div>
        </div>
      </div>
      ${room.description ? `
        <div>
          <div style="font-size:13px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;">Deskripsi</div>
          <p style="font-size:14px;line-height:1.6;color:var(--text-primary);margin:0;">${room.description}</p>
        </div>
      ` : ''}
      ${facilityBadges ? `
        <div>
          <div style="font-size:13px;font-weight:700;color:var(--text-muted);text-transform:uppercase;letter-spacing:1px;margin-bottom:8px;">Fasilitas</div>
          <div style="display:flex;flex-wrap:wrap;gap:8px;">${facilityBadges}</div>
        </div>
      ` : ''}
    </div>`;
}

function renderRoomCard(room) {
  const statusColor = room.status === 'AVAILABLE' ? 'var(--success)' : room.status === 'OCCUPIED' ? 'var(--danger)' : 'var(--warning)';
  const statusLabel = room.status === 'AVAILABLE' ? 'Tersedia' : room.status === 'OCCUPIED' ? 'Terisi' : 'Maintenance';
  const facilityBadges = (room.facilities || []).slice(0,4).map(f =>
    `<span style="background:#f1f5f9;color:#475569;padding:3px 8px;border-radius:20px;font-size:11px;">${f}</span>`
  ).join('');

  return `
    <div style="border:1px solid var(--border-light,#e2e8f0);border-radius:12px;padding:16px;background:var(--bg-card,#fff);">
      <div style="display:flex;justify-content:space-between;align-items:start;margin-bottom:10px;">
        <div>
          <div style="font-size:16px;font-weight:800;color:var(--text-primary)">Kamar ${room.roomNumber}</div>
          <div style="font-size:12px;color:var(--text-muted)">${room.roomType || ''}</div>
        </div>
        <span style="background:${statusColor}20;color:${statusColor};padding:4px 10px;border-radius:20px;font-size:11px;font-weight:700;">${statusLabel}</span>
      </div>
      ${room.description ? `<p style="font-size:13px;color:var(--text-muted);margin:0 0 10px;line-height:1.5;">${room.description.substring(0,80)}${room.description.length>80?'...':''}</p>` : ''}
      <div style="font-size:13px;font-weight:700;color:var(--text-muted);margin-bottom:6px;">Kapasitas: ${room.capacity} orang</div>
      <div style="display:flex;flex-wrap:wrap;gap:4px;margin-bottom:10px;">${facilityBadges}</div>
      <div style="font-size:16px;font-weight:800;color:var(--primary)">Rp ${fmt(room.price || 0)}<span style="font-size:11px;color:var(--text-muted);font-weight:400">/bulan</span></div>
    </div>`;
}

// ---- LOGOUT ----
function doLogout() {
  sessionStorage.clear();
  window.location.href = '/login';
}

// ---- HELPERS ----
function fmt(n) { return new Intl.NumberFormat('id-ID').format(n); }
function formatDateMonth(d) {
  if (!d) return 'Bulan Ini';
  return new Date(d).toLocaleDateString('id-ID', { month:'long', year:'numeric' });
}
function statusBadge(s) {
  const map = {
    UNPAID:               '<span class="sk-badge sk-badge-danger">BELUM BAYAR</span>',
    PENDING:              '<span class="sk-badge sk-badge-warning">PENDING</span>',
    WAITING_CONFIRMATION: '<span class="sk-badge sk-badge-info">MENUNGGU VERIFIKASI</span>',
    PAID:                 '<span class="sk-badge sk-badge-success">LUNAS</span>',
    CANCELLED:            '<span class="sk-badge sk-badge-danger">DIBATALKAN</span>',
    OVERDUE:              '<span class="sk-badge sk-badge-danger">TELAT BAYAR</span>',
    REJECTED:             '<span class="sk-badge sk-badge-danger">DITOLAK</span>',
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

// ---- COMPLAINTS LOGIC ----
async function loadComplaints() {
  const tbody = document.getElementById('complaintTable');
  if (!tbody || !userId) return;
  try {
    const res = await fetchWithToken(`${API}/api/complaints`, {
      headers: {
        'X-USER-ID': userId,
        'X-USER-ROLE': 'TENANT'
      }
    });
    const complaints = await res.json();
    if (!complaints || !complaints.length) {
      tbody.innerHTML = `<tr><td colspan="3"><div class="sk-empty"><div class="empty-icon"><i class="fa-solid fa-folder-open"></i></div><p>Tidak ada riwayat keluhan.</p></div></td></tr>`;
      return;
    }
    tbody.innerHTML = complaints.map(c => {
      let badge = '';
      if (c.status === 'PENDING') badge = '<span class="sk-badge sk-badge-warning">PENDING</span>';
      else if (c.status === 'PROCESS') badge = '<span class="sk-badge sk-badge-info">PROSES</span>';
      else if (c.status === 'DONE') badge = '<span class="sk-badge sk-badge-success">SELESAI</span>';
      else badge = '<span class="sk-badge sk-badge-danger">DITOLAK</span>';

      return `
        <tr>
          <td><strong>${c.title || '—'}</strong></td>
          <td>${c.description || '—'}</td>
          <td>${badge}</td>
        </tr>
      `;
    }).join('');
  } catch (e) {
    tbody.innerHTML = `<tr><td colspan="3" style="text-align:center; padding: 20px;">Gagal memuat keluhan.</td></tr>`;
  }
}

function initComplaintForm() {
  const form = document.getElementById('complaintForm');
  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const title = document.getElementById('compTitle').value.trim();
    const description = document.getElementById('compDesc').value.trim();

    if (!title || !description) {
      showToast('Harap isi judul dan deskripsi keluhan.', 'error');
      return;
    }

    if (!myRoomId) {
      showToast('Anda tidak dapat mengirim keluhan karena belum terdaftar di kamar manapun.', 'error');
      return;
    }

    const submitBtn = form.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    const originalText = submitBtn.innerHTML;
    submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Mengirim...';

    try {
      const res = await fetchWithToken(`${API}/api/complaints`, {
        method: 'POST',
        headers: {
          'X-USER-ID': userId
        },
        body: JSON.stringify({
          roomId: myRoomId,
          title,
          description
        })
      });

      if (!res.ok) {
        throw new Error('Gagal');
      }

      showToast('Keluhan berhasil dikirim!', 'success');
      form.reset();
      loadComplaints();
    } catch {
      showToast('Gagal mengirim keluhan.', 'error');
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = originalText;
    }
  });
}

// ---- EDIT PROFILE LOGIC ----
function openEditProfile() {
  document.getElementById('editProfileName').value = sessionStorage.getItem('username') || (myTenant ? myTenant.name : '');
  document.getElementById('editProfileEmail').value = sessionStorage.getItem('email') || (myTenant ? myTenant.email : '');
  document.getElementById('editProfilePhone').value = myTenant ? myTenant.phone : '';
  document.getElementById('editProfileNik').value = myTenant ? (myTenant.nik || '') : '';
  document.getElementById('editProfileAddress').value = myTenant ? (myTenant.address || '') : '';
  document.getElementById('editProfileModal').style.display = 'flex';
}

function closeEditProfile() {
  document.getElementById('editProfileModal').style.display = 'none';
}

async function submitEditProfile(e) {
  e.preventDefault();
  const name = document.getElementById('editProfileName').value.trim();
  const email = document.getElementById('editProfileEmail').value.trim();
  const phone = document.getElementById('editProfilePhone').value.trim();
  const nik = document.getElementById('editProfileNik').value.trim();
  const address = document.getElementById('editProfileAddress').value.trim();
  
  const btn = document.getElementById('btnSubmitProfile');

  if (!name || !email) {
    showToast('Harap isi nama dan email.', 'error');
    return;
  }

  btn.disabled = true;
  btn.textContent = 'Menyimpan...';

  try {
    // Update Auth Service
    const resAuth = await fetchWithToken(`${API}/api/auth/update`, {
      method: 'PUT',
      body: JSON.stringify({ username: name, email: email })
    });

    if (!resAuth.ok) {
      const err = await resAuth.json().catch(() => ({}));
      throw new Error(err.message || 'Gagal memperbarui profil auth');
    }
    const authData = await resAuth.json();
    sessionStorage.setItem('username', authData.username);
    
    // Update Tenant Service if tenant exists
    if (myTenant) {
      const tenantPayload = {
        userId: userId,
        roomId: myTenant.roomId,
        name: name,
        email: email,
        phone: phone,
        nik: nik,
        address: address,
        occupation: myTenant.occupation || 'Mahasiswa/Karyawan',
        emergencyContact: myTenant.emergencyContact || '-',
        startDate: myTenant.startDate,
        endDate: myTenant.endDate,
        monthlyRent: myTenant.monthlyRent
      };
      
      const resTenant = await fetchWithToken(`${API}/api/tenants/${myTenant.id}`, {
        method: 'PUT',
        body: JSON.stringify(tenantPayload)
      });
      
      if (!resTenant.ok) {
        throw new Error('Gagal memperbarui data tenant');
      }
      
      // Update local cache
      const updatedTenant = await resTenant.json();
      myTenant = updatedTenant;
    }
    sessionStorage.setItem('email', authData.email);
    
    initUser(); // Update UI
    closeEditProfile();
    showToast('Profil berhasil diperbarui!', 'success');
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Simpan Perubahan';
  }
}