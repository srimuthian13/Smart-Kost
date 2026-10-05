// ---- FETCH & RENDER ROOMS ----
document.addEventListener('DOMContentLoaded', () => {
  loadRooms();
  
  // Handle dynamic auth navigation
  const authNavLinks = document.getElementById('authNavLinks');
  if (authNavLinks) {
    const token = sessionStorage.getItem('accessToken');
    const role = sessionStorage.getItem('role');
    const username = sessionStorage.getItem('username') || 'User';
    
    if (token) {
      let dashUrl = '/dashboard/tenant';
      if (role === 'ADMIN') dashUrl = '/dashboard/admin';
      
      authNavLinks.innerHTML = `
        <a href="${dashUrl}" class="btn btn-outline" style="display:inline-flex;align-items:center;gap:4px;"><span class="material-symbols-outlined" style="font-size:16px;">account_circle</span> Profil ${username}</a>
        <button onclick="logoutIndex()" class="btn btn-cta" style="border:none;cursor:pointer;font-family:inherit;">Logout</button>
      `;
    }
  }

  // Auto-trigger rent modal if redirected from login
  const triggerRoomId = localStorage.getItem('triggerRentRoomId');
  if (triggerRoomId) {
    localStorage.removeItem('triggerRentRoomId');
    setTimeout(() => {
      openRoomDetail(triggerRoomId);
    }, 600);
  }

  const navToggle = document.getElementById('navToggle');
  if (navToggle) {
    navToggle.addEventListener('click', () => {
      document.getElementById('navLinks')?.classList.toggle('active');
    });
  }
});

function logoutIndex() {
  sessionStorage.clear();
  window.location.reload();
}

const iconMap = {
  'AC': 'ac_unit',
  'WiFi': 'wifi',
  'Kasur': 'bed',
  'Lemari': 'inventory_2',
  'Kamar Mandi Dalam': 'shower',
  'Meja Belajar': 'desk',
  'Kipas Angin': 'air',
  'TV': 'tv',
  'Dapur Mini': 'kitchen',
  'Water Heater': 'water_heater'
};

const defaultImages = [
  'https://images.unsplash.com/photo-1522771731478-44bf10cb16af?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
  'https://images.unsplash.com/photo-1555854877-bab0e564b8d5?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80',
  'https://images.unsplash.com/photo-1598928506311-c55ded91a20c?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
];

let currentRoomDetail = null;
let galleryCurrentIdx = 0;
let galleryImages = [];

async function loadRooms() {
  const gallery = document.getElementById('roomGallery');
  if (!gallery) return;

  try {
    const res = await fetch('http://localhost:8080/api/rooms?size=100');
    if (!res.ok) throw new Error('Failed to fetch rooms');
    
    const data = await res.json();
    const rooms = data.content || [];
    
    if (rooms.length > 0) {
      // Replace ALL content (removes dummy cards) with API data
      gallery.innerHTML = rooms.map((room, idx) => {
        const isAvail = room.status === 'AVAILABLE';
        const badgeClass = isAvail ? 'available' : 'full';
        const badgeText = isAvail ? 'Tersedia' : 'Penuh';
        
        // Use uploaded image if exists, otherwise fallback to Unsplash
        const imgUrl = (room.images && room.images.length > 0 && room.images[0])
          ? room.images[0]
          : defaultImages[idx % defaultImages.length];

        const facilitiesList = (room.facilities || []).slice(0, 4).map(fac => {
          const icon = iconMap[fac] || 'check_circle';
          return `<li><span class="material-symbols-outlined">${icon}</span> ${fac}</li>`;
        }).join('');

        const photoCount = room.images && room.images.length > 1
          ? `<div style="position:absolute;bottom:8px;right:8px;background:rgba(0,0,0,0.6);color:#fff;font-size:11px;padding:3px 8px;border-radius:20px;"><span class="material-symbols-outlined" style="font-size:13px;vertical-align:middle">photo_library</span> ${room.images.length} foto</div>`
          : '';

        return `
          <div class="room-card">
            <div style="position: relative; overflow: hidden; border-radius: 8px 8px 0 0;">
              <img src="${imgUrl}" alt="${room.roomType}" class="room-img"
                onerror="this.onerror=null;this.src='${defaultImages[idx % defaultImages.length]}'" />
              <span class="status-badge ${badgeClass}" style="position: absolute; top: 12px; right: 12px;">${badgeText}</span>
              ${photoCount}
            </div>
            <div class="room-content">
              <div class="room-title">Tipe ${room.roomType} (#${room.roomNumber})</div>
              <div class="room-price">
                Rp ${fmt(room.price || 0)} <span style="font-size:12px;color:var(--text-muted)">/ bulan</span>
              </div>
              <ul class="room-features">
                ${facilitiesList || '<li><span class="material-symbols-outlined">info</span> Standar</li>'}
              </ul>
              <button onclick="openRoomDetail(${room.id})" class="btn btn-primary" style="width: 100%; justify-content: center;">Lihat Detail</button>
            </div>
          </div>
        `;
      }).join('');
    } else {
      // No rooms yet — clear dummy & show empty state
      gallery.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px; color: var(--text-muted);">
          <span class="material-symbols-outlined" style="font-size: 56px; display: block; margin-bottom: 12px; color: var(--primary); opacity: 0.5;">hotel</span>
          <p style="font-size: 16px;">Belum ada kamar yang tersedia. Silakan hubungi Admin.</p>
        </div>
      `;
    }
  } catch (err) {
    console.warn('Backend rooms API gagal, menampilkan kartu default:', err);
    // Keep existing dummy HTML cards as fallback
  }
}

// ---- IMAGE GALLERY NAVIGATION ----
function galleryNav(direction) {
  galleryCurrentIdx = (galleryCurrentIdx + direction + galleryImages.length) % galleryImages.length;
  updateGalleryDisplay();
}

function galleryGoTo(idx) {
  galleryCurrentIdx = idx;
  updateGalleryDisplay();
}

function updateGalleryDisplay() {
  const mainImg = document.getElementById('galleryMainImg');
  if (mainImg) {
    mainImg.style.opacity = '0';
    setTimeout(() => {
      mainImg.src = galleryImages[galleryCurrentIdx];
      mainImg.style.opacity = '1';
    }, 150);
  }
  document.querySelectorAll('.gallery-thumb').forEach((thumb, i) => {
    thumb.style.border = i === galleryCurrentIdx ? '2px solid var(--primary)' : '2px solid transparent';
    thumb.style.opacity = i === galleryCurrentIdx ? '1' : '0.6';
  });
  const counter = document.getElementById('galleryCounter');
  if (counter) counter.textContent = `${galleryCurrentIdx + 1} / ${galleryImages.length}`;
}

async function openRoomDetail(roomId) {
  try {
    const res = await fetch(`http://localhost:8080/api/rooms/${roomId}`);
    if (!res.ok) throw new Error('Room not found');
    currentRoomDetail = await res.json();
    
    document.getElementById('detailTitle').textContent = `Kamar ${currentRoomDetail.roomNumber} — Tipe ${currentRoomDetail.roomType}`;
    
    const isAvail = currentRoomDetail.status === 'AVAILABLE';
    const badgeClass = isAvail ? 'available' : 'full';
    const badgeText = isAvail ? 'Tersedia' : 'Penuh';
    
    // Build images array — use uploaded photos or fallback
    galleryImages = (currentRoomDetail.images && currentRoomDetail.images.length > 0)
      ? currentRoomDetail.images
      : [defaultImages[0]];
    galleryCurrentIdx = 0;
    
    const facilitiesList = (currentRoomDetail.facilities || []).map(fac => {
      const icon = iconMap[fac] || 'check_circle';
      return `<li style="display:flex;align-items:center;gap:8px;padding:6px 0;border-bottom:1px solid rgba(201,166,107,0.1);font-size:13.5px;">
        <span class="material-symbols-outlined" style="font-size:18px;color:var(--primary)">${icon}</span> ${fac}
      </li>`;
    }).join('');
    
    const descriptionText = currentRoomDetail.description || 'Tidak ada deskripsi.';
    
    // Thumbnail strip (only if multiple images)
    const thumbsHtml = galleryImages.length > 1
      ? `<div style="display:flex;gap:8px;margin-top:8px;overflow-x:auto;padding-bottom:4px;">
          ${galleryImages.map((img, i) => `
            <img src="${img}" class="gallery-thumb" onclick="galleryGoTo(${i})"
              onerror="this.style.display='none'"
              style="width:64px;height:48px;object-fit:cover;border-radius:4px;cursor:pointer;flex-shrink:0;
                     border:${i===0?'2px solid var(--primary)':'2px solid transparent'};
                     opacity:${i===0?'1':'0.6'};transition:all 0.2s;" />
          `).join('')}
        </div>`
      : '';
    
    document.getElementById('detailBody').innerHTML = `
      <!-- Image Gallery -->
      <div style="position:relative;margin-bottom:16px;border-radius:8px;overflow:hidden;">
        <img id="galleryMainImg" src="${galleryImages[0]}" class="detail-img"
          alt="${currentRoomDetail.roomType}"
          style="transition:opacity 0.15s;width:100%;object-fit:cover;max-height:280px;border-radius:8px;"
          onerror="this.onerror=null;this.src='${defaultImages[0]}'" />
        ${galleryImages.length > 1 ? `
          <button onclick="galleryNav(-1)" style="position:absolute;left:8px;top:50%;transform:translateY(-50%);
            background:rgba(0,0,0,0.55);color:#fff;border:none;border-radius:50%;width:36px;height:36px;
            cursor:pointer;font-size:20px;display:flex;align-items:center;justify-content:center;z-index:2;">&#8249;</button>
          <button onclick="galleryNav(1)" style="position:absolute;right:8px;top:50%;transform:translateY(-50%);
            background:rgba(0,0,0,0.55);color:#fff;border:none;border-radius:50%;width:36px;height:36px;
            cursor:pointer;font-size:20px;display:flex;align-items:center;justify-content:center;z-index:2;">&#8250;</button>
          <div id="galleryCounter" style="position:absolute;bottom:10px;right:10px;background:rgba(0,0,0,0.55);
            color:#fff;font-size:12px;padding:3px 10px;border-radius:20px;">1 / ${galleryImages.length}</div>
        ` : ''}
      </div>
      ${thumbsHtml}

      <!-- Status & Capacity -->
      <div style="display:flex;justify-content:space-between;align-items:center;margin:14px 0 10px;">
        <span class="status-badge ${badgeClass}">${badgeText}</span>
        <div style="font-size:13px;color:var(--text-secondary);">
          <span class="material-symbols-outlined" style="font-size:16px;vertical-align:middle">person</span>
          Kapasitas: <strong>${currentRoomDetail.capacity} Orang</strong>
        </div>
      </div>

      <!-- Price -->
      <div class="detail-price" style="margin-bottom:16px;">
        Rp ${fmt(currentRoomDetail.price)}
        <span style="font-size:12px;color:var(--text-muted);font-weight:400"> / bulan</span>
      </div>

      <!-- Description -->
      <div style="margin-bottom:12px;padding:14px;background:rgba(201,166,107,0.06);border-radius:6px;border:1px solid rgba(201,166,107,0.15);">
        <h4 style="font-size:12px;color:var(--primary);margin-bottom:6px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;">Deskripsi</h4>
        <p style="font-size:13.5px;color:var(--text-primary);line-height:1.65;margin:0;">${descriptionText}</p>
      </div>

      <!-- Facilities -->
      <div style="padding:14px;background:rgba(201,166,107,0.06);border-radius:6px;border:1px solid rgba(201,166,107,0.15);">
        <h4 style="font-size:12px;color:var(--primary);margin-bottom:8px;font-weight:700;text-transform:uppercase;letter-spacing:0.5px;">Fasilitas</h4>
        <ul style="list-style:none;padding:0;margin:0;">
          ${facilitiesList || '<li style="color:var(--text-muted);font-size:13px;">Standar (tidak ada detail fasilitas)</li>'}
        </ul>
      </div>
    `;
    
    const waText = encodeURIComponent(`Halo admin, saya tertarik dengan Kamar ${currentRoomDetail.roomNumber} Tipe ${currentRoomDetail.roomType} (Rp ${fmt(currentRoomDetail.price)}/bulan). Apakah masih tersedia?`);
    document.getElementById('detailWaBtn').href = `https://wa.me/6281234567890?text=${waText}`;
    
    const sewaBtn = document.getElementById('detailSewaBtn');
    if (isAvail) {
      sewaBtn.disabled = false;
      sewaBtn.style.opacity = '1';
      sewaBtn.style.cursor = 'pointer';
      sewaBtn.textContent = 'Ajukan Sewa';
    } else {
      sewaBtn.disabled = true;
      sewaBtn.style.opacity = '0.5';
      sewaBtn.style.cursor = 'not-allowed';
      sewaBtn.textContent = 'Tidak Tersedia';
    }
    
    openModal('roomDetailModal');
  } catch (err) {
    console.error(err);
    alert('Gagal memuat detail kamar');
  }
}

function handleRentClick() {
  if (!currentRoomDetail) return;

  // No authentication required; directly open rent modal
  closeModal('roomDetailModal');

  document.getElementById('rentRoomId').value = currentRoomDetail.id;
  document.getElementById('rentRoomPrice').value = currentRoomDetail.price;
  document.getElementById('rentRoomNumber').value = currentRoomDetail.roomNumber;
  document.getElementById('rentRoomType').value = currentRoomDetail.roomType;

  document.getElementById('rentRoomSummary').textContent = `Kamar ${currentRoomDetail.roomNumber} (${currentRoomDetail.roomType}) — Rp ${fmt(currentRoomDetail.price)} / bulan`;
  // Prefill name if available in sessionStorage (optional)
  document.getElementById('rentName').value = sessionStorage.getItem('username') || '';

  openModal('rentModal');
}

document.getElementById('rentForm')?.addEventListener('submit', async (e) => {
  e.preventDefault();
  
  const alertEl = document.getElementById('rentAlert');
  alertEl.style.display = 'none';
  
  const submitBtn = document.getElementById('btnRentSubmit');
  submitBtn.disabled = true;
  submitBtn.textContent = 'Mengirim Pengajuan...';
  
  const roomId = parseInt(document.getElementById('rentRoomId').value);
  const price = parseFloat(document.getElementById('rentRoomPrice').value);
  const roomNumber = document.getElementById('rentRoomNumber').value;
  
  const name = document.getElementById('rentName').value;
  const phone = document.getElementById('rentPhone').value;
  const nik = document.getElementById('rentNik').value;
  const address = document.getElementById('rentAddress').value;
  const occupation = document.getElementById('rentOccupation').value;
  const duration = parseInt(document.getElementById('rentDuration').value);
  const email = sessionStorage.getItem('email');
  
  const start = new Date();
  const startDate = start.toISOString().split('T')[0];
  
  const end = new Date(start);
  end.setMonth(start.getMonth() + duration);
  const endDate = end.toISOString().split('T')[0];
  
  try {
    const token = sessionStorage.getItem('accessToken');
    
    // 1. Create Tenant in tenant_service
    const tenantRes = await fetch('http://localhost:8080/api/tenants', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify({
        userId: sessionStorage.getItem('userId'),
        roomId,
        name,
        phone,
        email,
        nik,
        address,
        occupation: occupation,
        emergencyContact: '-',
        startDate,
        endDate,
        monthlyRent: price
      })
    });
    
    if (!tenantRes.ok) {
      const err = await tenantRes.json().catch(() => ({}));
      throw new Error(err.message || 'Gagal mendaftarkan tenant. Pastikan NIK Anda belum terdaftar.');
    }
    
    const tenantData = await tenantRes.json();
    
    // 2. Create Payment in payment_service
    const invoiceRes = await fetch('http://localhost:8080/api/payments', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
      },
      body: JSON.stringify({
        tenantId: tenantData.id,
        roomId: roomId,
        amount: price,
        dueDate: startDate,
        paymentDate: new Date().toISOString().split('T')[0],
        paymentMethod: 'Menunggu'
      })
    });
    
    if (!invoiceRes.ok) {
      throw new Error('Gagal membuat tagihan sewa.');
    }
    
    const invoiceData = await invoiceRes.json();
    
    closeModal('rentModal');
    window.location.href = `/dashboard/tenant/payment?invoiceId=${invoiceData.id}`;
    
  } catch (err) {
    alertEl.textContent = err.message;
    alertEl.style.display = 'block';
    submitBtn.disabled = false;
    submitBtn.textContent = 'Kirim Pengajuan';
  }
});

function openModal(id) {
  document.getElementById(id).classList.add('active');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('active');
}

function fmt(n) {
  return new Intl.NumberFormat('id-ID').format(n);
}
