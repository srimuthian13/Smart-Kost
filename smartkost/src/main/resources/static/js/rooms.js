/* =============================================
   ROOMS MANAGEMENT — Smart Kost Admin/Owner
   ============================================= */

const API_ROOMS = 'http://localhost:8080/api/rooms';
let allRooms  = [];
let deleteTargetId = null;

const defaultFacilities = ['Kasur'];
let addFacilitiesList = [...defaultFacilities];
let editFacilitiesList = [...defaultFacilities];
let selectedEditFacilities = [];

// ---- INIT ----
document.addEventListener('DOMContentLoaded', () => {
  if (!getToken()) {
    window.location.href = '/login';
    return;
  }



  loadRooms();
  initSearch();
  
  addFacilitiesList = [...defaultFacilities];
  renderFacilitiesList('add');
});

// ---- AUTH ----
function getToken() {
  return sessionStorage.getItem('accessToken');
}

function authHeaders() {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
}

// ---- LOAD ROOMS ----
async function loadRooms() {
  const tbody = document.getElementById('roomTableBody');
  tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-muted">
    <span class="material-symbols-outlined d-block mb-2" style="font-size:40px">hourglass_empty</span>Memuat data kamar...
  </td></tr>`;

  try {
    const res = await fetch(`${API_ROOMS}?size=1000&sort=roomNumber,asc`, {
      headers: authHeaders()
    });

    if (res.status === 401 || res.status === 403) {
      showAlert('Sesi Anda habis. Silakan login ulang.', 'danger');
      setTimeout(() => window.location.href = '/login', 1500);
      return;
    }
    if (!res.ok) throw new Error(`Error ${res.status}`);

    const data = await res.json();
    allRooms = data.content || data || [];

    updateStats();
    renderTable(allRooms);

    const urlParams = new URLSearchParams(window.location.search);
    const editId = urlParams.get('edit');
    if (editId) {
      setTimeout(() => openEdit(parseInt(editId)), 300);
      window.history.replaceState({}, document.title, window.location.pathname);
    }

  } catch (err) {
    console.error('Load rooms error:', err);
    tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-danger">
      <span class="material-symbols-outlined d-block mb-2" style="font-size:40px">error</span>
      Gagal memuat data kamar. Pastikan room_service berjalan.<br>
      <small class="text-muted">${err.message}</small>
    </td></tr>`;
  }
}

// ---- RENDER TABLE ----
function renderTable(rooms) {
  const tbody = document.getElementById('roomTableBody');
  const statusFilter = document.getElementById('statusFilter').value;
  const search = document.getElementById('searchRoom').value.toLowerCase();

  const filtered = rooms.filter(r => {
    const matchStatus = !statusFilter || r.status === statusFilter;
    const matchSearch = !search ||
      r.roomNumber.toLowerCase().includes(search) ||
      r.roomType.toLowerCase().includes(search);
    return matchStatus && matchSearch;
  });

  if (!filtered.length) {
    tbody.innerHTML = `<tr><td colspan="8" class="text-center py-5 text-muted">
      <span class="material-symbols-outlined d-block mb-2" style="font-size:40px">search_off</span>
      Tidak ada kamar yang ditemukan.
    </td></tr>`;
    return;
  }

  tbody.innerHTML = filtered.map((room, idx) => {
    const statusClass = {
      'AVAILABLE': 'badge-available',
      'OCCUPIED': 'badge-occupied',
      'UNDER_MAINTENANCE': 'badge-maintenance'
    }[room.status] || 'bg-secondary';

    const statusLabel = {
      'AVAILABLE': 'Tersedia',
      'OCCUPIED': 'Terisi',
      'UNDER_MAINTENANCE': 'Maintenance'
    }[room.status] || room.status;

    const facilities = (room.facilities || []).join(', ') || '—';

    return `
      <tr>
        <td>${idx + 1}</td>
        <td><strong>${room.roomNumber}</strong></td>
        <td>${room.roomType}</td>
        <td>Rp ${fmt(room.price)}</td>
        <td>${room.capacity} orang</td>
        <td><small class="text-muted">${facilities}</small></td>
        <td><span class="status-pill ${statusClass}">${statusLabel}</span></td>
        <td>
          <div class="d-flex gap-2">
            <button class="btn-action btn-edit" onclick="openEdit(${room.id})" title="Edit">
              <span class="material-symbols-outlined">edit</span>
            </button>
            <button class="btn-action btn-delete" onclick="openDelete(${room.id}, '${room.roomNumber}')" title="Hapus">
              <span class="material-symbols-outlined">delete</span>
            </button>
          </div>
        </td>
      </tr>`;
  }).join('');
}

// ---- STATS ----
function updateStats() {
  document.getElementById('totalRooms').textContent = allRooms.length;
  document.getElementById('availableRooms').textContent = allRooms.filter(r => r.status === 'AVAILABLE').length;
  document.getElementById('occupiedRooms').textContent = allRooms.filter(r => r.status === 'OCCUPIED').length;
  document.getElementById('maintenanceRooms').textContent = allRooms.filter(r => r.status === 'UNDER_MAINTENANCE').length;
}

// ---- SEARCH / FILTER ----
function initSearch() {
  document.getElementById('searchRoom').addEventListener('input', () => renderTable(allRooms));
  document.getElementById('statusFilter').addEventListener('change', () => renderTable(allRooms));
}

// ---- CREATE ROOM ----
async function createRoom() {
  const roomNumber = document.getElementById('addRoomNumber').value.trim();
  const roomType   = document.getElementById('addRoomType').value.trim();
  const price      = parseFloat(document.getElementById('addPrice').value);
  const capacity   = parseInt(document.getElementById('addCapacity').value);
  const description= document.getElementById('addDescription').value.trim();
  const facilities = [...document.querySelectorAll('.add-facility:checked')].map(c => c.value);
  const roomImageFile = document.getElementById('addRoomImage').files[0];

  // Client-side validation
  const addAlert = document.getElementById('addAlert');
  if (!roomNumber) return showModalAlert(addAlert, 'Nomor kamar wajib diisi.');
  if (roomNumber.length > 20) return showModalAlert(addAlert, 'Nomor kamar maksimal 20 karakter.');
  if (!roomType) return showModalAlert(addAlert, 'Tipe kamar wajib diisi.');
  if (roomType.length > 50) return showModalAlert(addAlert, 'Tipe kamar maksimal 50 karakter.');
  if (!price || price < 50000) return showModalAlert(addAlert, 'Harga minimal Rp 50.000.');
  if (price > 99999999) return showModalAlert(addAlert, 'Harga maksimal Rp 99.999.999.');
  if (!capacity || capacity < 1) return showModalAlert(addAlert, 'Kapasitas minimal 1 orang.');
  if (capacity > 10) return showModalAlert(addAlert, 'Kapasitas maksimal 10 orang.');
  if (description.length > 500) return showModalAlert(addAlert, 'Deskripsi maksimal 500 karakter.');
  if (!roomImageFile) return showModalAlert(addAlert, 'Gambar kamar wajib diunggah.');

  setBtnLoading('addBtnText', 'addBtnLoader', true);
  addAlert.classList.add('d-none');

  try {
    const res = await fetch(API_ROOMS, {
      method: 'POST',
      headers: authHeaders(),
      body: JSON.stringify({ roomNumber, roomType, price, capacity, description, facilities })
    });

    if (res.status === 401 || res.status === 403) {
      return showModalAlert(addAlert, 'Anda tidak memiliki akses untuk menambah kamar.');
    }
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      return showModalAlert(addAlert, err.message || `Gagal menyimpan kamar (${res.status}).`);
    }

    const createdRoom = await res.json();

    // Upload image
    const formData = new FormData();
    formData.append('images', roomImageFile);

    const imgRes = await fetch(`${API_ROOMS}/${createdRoom.id}/images`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getToken()}`
      },
      body: formData
    });

    if (!imgRes.ok) {
      const imgErr = await imgRes.json().catch(() => ({}));
      return showModalAlert(addAlert, imgErr.message || `Gagal mengunggah gambar kamar (${imgRes.status}).`);
    }

    bootstrap.Modal.getInstance(document.getElementById('addRoomModal'))?.hide();
    document.getElementById('addRoomForm').reset();
    document.querySelectorAll('.add-facility').forEach(c => c.checked = false);
    showToast('✅ Kamar berhasil ditambahkan!', 'success');
    loadRooms();

  } catch (err) {
    showModalAlert(addAlert, 'Terjadi kesalahan jaringan. Coba lagi.');
  } finally {
    setBtnLoading('addBtnText', 'addBtnLoader', false);
  }
}

// ---- OPEN EDIT ----
function openEdit(id) {
  const room = allRooms.find(r => r.id === id);
  if (!room) return;

  document.getElementById('editRoomId').value    = room.id;
  document.getElementById('editRoomNumber').value = room.roomNumber;
  document.getElementById('editRoomType').value   = room.roomType;
  document.getElementById('editPrice').value      = room.price;
  document.getElementById('editCapacity').value   = room.capacity;
  document.getElementById('editDescription').value= room.description || '';
  document.getElementById('editAlert').classList.add('d-none');

  const editFacilities = room.facilities || [];
  selectedEditFacilities = [...editFacilities];
  editFacilitiesList = Array.from(new Set([...defaultFacilities, ...editFacilities]));
  renderFacilitiesList('edit');

  document.getElementById('editRoomImage').value = '';
  const editPreviewContainer = document.getElementById('editRoomImagePreviewContainer');
  const editPreview = document.getElementById('editRoomImagePreview');
  if (room.images && room.images.length > 0) {
    editPreview.src = room.images[0];
    editPreviewContainer.style.display = 'block';
  } else {
    editPreviewContainer.style.display = 'none';
    editPreview.src = '';
  }

  new bootstrap.Modal(document.getElementById('editRoomModal')).show();
}

// ---- UPDATE ROOM ----
async function updateRoom() {
  const id         = document.getElementById('editRoomId').value;
  const roomNumber = document.getElementById('editRoomNumber').value.trim();
  const roomType   = document.getElementById('editRoomType').value.trim();
  const price      = parseFloat(document.getElementById('editPrice').value);
  const capacity   = parseInt(document.getElementById('editCapacity').value);
  const description= document.getElementById('editDescription').value.trim();
  const facilities = [...document.querySelectorAll('.edit-facility:checked')].map(c => c.value);
  const roomImageFile = document.getElementById('editRoomImage').files[0];

  const editAlert = document.getElementById('editAlert');
  if (!roomNumber) return showModalAlert(editAlert, 'Nomor kamar wajib diisi.');
  if (roomNumber.length > 20) return showModalAlert(editAlert, 'Nomor kamar maksimal 20 karakter.');
  if (!roomType) return showModalAlert(editAlert, 'Tipe kamar wajib diisi.');
  if (roomType.length > 50) return showModalAlert(editAlert, 'Tipe kamar maksimal 50 karakter.');
  if (!price || price < 50000) return showModalAlert(editAlert, 'Harga minimal Rp 50.000.');
  if (price > 99999999) return showModalAlert(editAlert, 'Harga maksimal Rp 99.999.999.');
  if (!capacity || capacity < 1) return showModalAlert(editAlert, 'Kapasitas minimal 1 orang.');
  if (capacity > 10) return showModalAlert(editAlert, 'Kapasitas maksimal 10 orang.');
  if (description.length > 500) return showModalAlert(editAlert, 'Deskripsi maksimal 500 karakter.');

  setBtnLoading('editBtnText', 'editBtnLoader', true);
  editAlert.classList.add('d-none');

  try {
    const res = await fetch(`${API_ROOMS}/${id}`, {
      method: 'PUT',
      headers: authHeaders(),
      body: JSON.stringify({ roomNumber, roomType, price, capacity, description, facilities })
    });

    if (res.status === 401 || res.status === 403) {
      return showModalAlert(editAlert, 'Anda tidak memiliki akses untuk mengubah kamar.');
    }
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      return showModalAlert(editAlert, err.message || `Gagal memperbarui kamar (${res.status}).`);
    }

    if (roomImageFile) {
      const formData = new FormData();
      formData.append('images', roomImageFile);

      const imgRes = await fetch(`${API_ROOMS}/${id}/images`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${getToken()}`
        },
        body: formData
      });

      if (!imgRes.ok) {
        const imgErr = await imgRes.json().catch(() => ({}));
        return showModalAlert(editAlert, imgErr.message || `Gagal mengunggah gambar kamar baru (${imgRes.status}).`);
      }
    }

    bootstrap.Modal.getInstance(document.getElementById('editRoomModal'))?.hide();
    showToast('✅ Kamar berhasil diperbarui!', 'success');
    loadRooms();

  } catch (err) {
    showModalAlert(editAlert, 'Terjadi kesalahan jaringan. Coba lagi.');
  } finally {
    setBtnLoading('editBtnText', 'editBtnLoader', false);
  }
}

// ---- DELETE ----
function openDelete(id, roomNumber) {
  deleteTargetId = id;
  document.getElementById('deleteRoomName').textContent = `#${roomNumber}`;
  new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

async function confirmDelete() {
  if (!deleteTargetId) return;
  setBtnLoading('delBtnText', 'delBtnLoader', true);

  try {
    const res = await fetch(`${API_ROOMS}/${deleteTargetId}`, {
      method: 'DELETE',
      headers: authHeaders()
    });

    if (res.status === 401 || res.status === 403) {
      showAlert('Anda tidak memiliki akses untuk menghapus kamar.', 'danger');
      return;
    }
    if (!res.ok && res.status !== 204) throw new Error(`Error ${res.status}`);

    bootstrap.Modal.getInstance(document.getElementById('deleteModal'))?.hide();
    showToast('🗑️ Kamar berhasil dihapus.', 'warning');
    deleteTargetId = null;
    loadRooms();

  } catch (err) {
    showAlert('Gagal menghapus kamar. Coba lagi.', 'danger');
  } finally {
    setBtnLoading('delBtnText', 'delBtnLoader', false);
  }
}

// ---- HELPERS ----
function fmt(n) {
  return new Intl.NumberFormat('id-ID').format(n);
}

function showToast(msg, type = 'success') {
  const toastEl = document.getElementById('liveToast');
  const toastMsg = document.getElementById('toastMsg');
  toastEl.className = `toast align-items-center border-0 text-bg-${type === 'success' ? 'success' : type === 'warning' ? 'warning' : 'danger'}`;
  toastMsg.textContent = msg;
  new bootstrap.Toast(toastEl, { delay: 3500 }).show();
}

function showAlert(msg, type = 'danger') {
  const box = document.getElementById('alertBox');
  box.className = `alert alert-${type} d-flex align-items-center gap-2`;
  box.innerHTML = `<span class="material-symbols-outlined">${type === 'success' ? 'check_circle' : 'error'}</span> ${msg}`;
  box.classList.remove('d-none');
  setTimeout(() => box.classList.add('d-none'), 5000);
}

function showModalAlert(el, msg) {
  el.textContent = msg;
  el.classList.remove('d-none');
}

function setBtnLoading(textId, loaderId, loading) {
  const text = document.getElementById(textId);
  const loader = document.getElementById(loaderId);
  if (text) text.style.display = loading ? 'none' : '';
  if (loader) loader.classList.toggle('d-none', !loading);
  const btn = loader?.closest('button');
  if (btn) btn.disabled = loading;
}

// ---- DYNAMIC FACILITIES HELPERS ----
function renderFacilitiesList(type) {
  const container = document.getElementById(type === 'add' ? 'addFacilitiesContainer' : 'editFacilitiesContainer');
  if (!container) return;
  
  const list = type === 'add' ? addFacilitiesList : editFacilitiesList;
  const inputClass = type === 'add' ? 'add-facility' : 'edit-facility';
  
  container.innerHTML = list.map((fac, idx) => {
    let isChecked = false;
    if (type === 'add') {
      const existingInput = document.querySelector(`.add-facility[value="${fac}"]`);
      isChecked = existingInput ? existingInput.checked : true;
    } else {
      isChecked = selectedEditFacilities.includes(fac);
    }
    
    const id = `${type}_fac_${idx}`;
    return `
      <div class="col-6 col-md-4 d-flex align-items-center justify-content-between mb-1" style="background: rgba(201, 166, 107, 0.05); padding: 4px 8px; border-radius: 4px; border: 1px solid rgba(201, 166, 107, 0.15);">
        <div class="form-check m-0">
          <input class="form-check-input ${inputClass}" type="checkbox" value="${fac}" id="${id}" ${isChecked ? 'checked' : ''} onchange="onFacilityCheckChange('${type}', '${fac}', this.checked)">
          <label class="form-check-label text-light" for="${id}" style="font-size: 13px; color: #F5F0E8 !important;">${fac}</label>
        </div>
        <button type="button" class="btn btn-link btn-sm text-danger p-0 border-0" onclick="removeFacility('${type}', '${fac}')" style="line-height:1; font-size:18px; text-decoration:none; color: #BFA88A !important;">&times;</button>
      </div>
    `;
  }).join('');
}

function onFacilityCheckChange(type, fac, checked) {
  if (type === 'edit') {
    if (checked) {
      if (!selectedEditFacilities.includes(fac)) selectedEditFacilities.push(fac);
    } else {
      selectedEditFacilities = selectedEditFacilities.filter(f => f !== fac);
    }
  }
}

function addCustomFacility(type) {
  const input = document.getElementById(type === 'add' ? 'addCustomFacilityInput' : 'editCustomFacilityInput');
  const val = input.value.trim();
  if (!val) return;
  
  if (type === 'add') {
    if (!addFacilitiesList.includes(val)) {
      addFacilitiesList.push(val);
    }
  } else {
    if (!editFacilitiesList.includes(val)) {
      editFacilitiesList.push(val);
    }
    if (!selectedEditFacilities.includes(val)) {
      selectedEditFacilities.push(val);
    }
  }
  
  input.value = '';
  renderFacilitiesList(type);
}

function removeFacility(type, fac) {
  if (type === 'add') {
    addFacilitiesList = addFacilitiesList.filter(f => f !== fac);
  } else {
    editFacilitiesList = editFacilitiesList.filter(f => f !== fac);
    selectedEditFacilities = selectedEditFacilities.filter(f => f !== fac);
  }
  renderFacilitiesList(type);
}

function previewImage(input, previewId) {
  const file = input.files[0];
  const container = document.getElementById(previewId + 'Container');
  const img = document.getElementById(previewId);
  
  if (file) {
    const reader = new FileReader();
    reader.onload = function(e) {
      img.src = e.target.result;
      container.style.display = 'block';
    }
    reader.readAsDataURL(file);
  } else {
    img.src = '';
    container.style.display = 'none';
  }
}
