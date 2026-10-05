// Get invoiceId from URL
const urlParams = new URLSearchParams(window.location.search);
const invId = urlParams.get('invoiceId');
  if (invId) {
    document.getElementById('invoiceId').value = invId;
    fetchInvoiceData(invId);
  } else {
    // If no ID passed, try to fetch the latest unpaid invoice from session userId
    const uid = sessionStorage.getItem('userId');
    if (uid) {
      const token = sessionStorage.getItem('accessToken');
      // Fetch tenant data by userId first
      fetch(`http://localhost:8080/api/tenants/user/${uid}`, { headers: { 'Authorization': `Bearer ${token}` } })
        .then(r => r.json())
        .then(tenants => {
           if (tenants && tenants.length) {
               // Find active or pending tenant
               const activeTenant = tenants.find(t => !t.checkOutDate);
               if (activeTenant) {
                   return fetch(`http://localhost:8080/api/payments/tenant/${activeTenant.id}`, { headers: { 'Authorization': `Bearer ${token}` } });
               }
           }
           throw new Error("No active tenant found");
        })
        .then(r => r.json())
        .then(data => {
          if (data && data.length) {
            // Find an UNPAID invoice instead of just checking the last item
            const unpaid = data.find(inv => !inv.status || inv.status === 'UNPAID');
            if (unpaid) {
              document.getElementById('invoiceId').value = unpaid.id;
              fetchInvoiceData(unpaid.id);
            } else {
              showToast('Tidak ada tagihan yang belum dibayar. Kembali ke dashboard.', 'info');
              setTimeout(()=>window.location.href='/dashboard/tenant', 2000);
            }
          }
        }).catch(e=>console.log(e));
    }
  }

function fetchInvoiceData(id) {
    const token = sessionStorage.getItem('accessToken');
    fetch(`http://localhost:8080/api/payments/${id}`, { headers: token ? { 'Authorization': `Bearer ${token}` } : {} })
        .then(r => r.json())
        .then(data => {
            const elAmount = document.getElementById('displayAmount');
            if(elAmount) elAmount.textContent = 'Rp ' + new Intl.NumberFormat('id-ID').format(data.amount);
            
            const elDate = document.getElementById('displayDueDate');
            if(elDate) elDate.textContent = data.dueDate;
        }).catch(e=>console.log(e));
}

function toggleMethod() {
  const method = document.getElementById('payMethod').value;
  if (method === 'QRIS') {
    document.getElementById('bankSection').style.display = 'none';
    document.getElementById('qrisSection').style.display = 'block';
    const id = document.getElementById('invoiceId').value;
    if (id) {
      const token = sessionStorage.getItem('accessToken');
      fetch(`http://localhost:8080/api/payments/${id}/qris`, {
        headers: token ? { 'Authorization': `Bearer ${token}` } : {}
      })
      .then(res => res.blob())
      .then(blob => {
        const url = URL.createObjectURL(blob);
        const imgEl = document.getElementById('qrisImg');
        imgEl.src = url;
        imgEl.style.cursor = 'pointer';
        
        // Fetch amount to construct simulation URL accurately 
        // We already have payment data from fetchInvoiceData but it's not global, 
        // so we'll just open the base URL with ID and let the page fetch or we can pass the ID
        // The Java backend encodes the amount in the QR string, but we can just pass paymentId here
        imgEl.onclick = () => {
           window.open(`http://localhost:8080/qris-simulate.html?paymentId=${id}`, '_blank');
        };
      })
      .catch(err => console.error('Gagal memuat QRIS', err));
    }
  } else {
    document.getElementById('bankSection').style.display = 'block';
    document.getElementById('qrisSection').style.display = 'none';
  }
}

function previewFile(e) {
  const file = e.target.files[0];
  const img = document.getElementById('previewImg');
  const nameLabel = document.getElementById('fileNamePreview');
  
  if (file) {
    nameLabel.textContent = file.name;
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = function(evt) {
        img.src = evt.target.result;
        img.style.display = 'block';
        img.style.width = '';
        img.style.height = '';
      }
      reader.readAsDataURL(file);
    } else {
      // Document placeholder icon
      img.src = 'https://cdn-icons-png.flaticon.com/512/337/337946.png';
      img.style.display = 'block';
      img.style.width = '64px';
      img.style.height = '64px';
      img.style.margin = '16px auto 0';
    }
  } else {
    img.style.display = 'none';
    nameLabel.textContent = '';
  }
}

document.getElementById('uploadForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  const id = document.getElementById('invoiceId').value;
  if (!id) {
    showToast('ID Invoice tidak ditemukan. Harap kembali ke dashboard.', 'error');
    return;
  }
  const fileInput = document.getElementById('proofFile');
  if (!fileInput.files.length) {
    showToast('Harap pilih file bukti pembayaran.', 'warning');
    return;
  }
  const method = document.getElementById('payMethod').value;
  
  const formData = new FormData();
  formData.append('file', fileInput.files[0]);
  formData.append('paymentMethod', method);

  const btn = document.getElementById('btnSubmit');
  btn.disabled = true;
  btn.innerHTML = '<span class="sk-loader"></span> Mengupload...';

  try {
    const token = sessionStorage.getItem('accessToken');
    const res = await fetch(`http://localhost:8080/api/payments/${id}/upload-proof`, {
      method: 'POST',
      headers: token ? { 'Authorization': `Bearer ${token}` } : {},
      body: formData
    });

    if (!res.ok) throw new Error('Gagal mengupload');
    
    showToast('Bukti pembayaran berhasil diupload!', 'success');
    setTimeout(() => {
      window.location.href = '/dashboard/tenant';
    }, 2000);
  } catch (err) {
    showToast('Terjadi kesalahan saat upload.', 'error');
    btn.disabled = false;
    btn.innerHTML = '<span class="material-symbols-outlined">cloud_upload</span> Upload Bukti Pembayaran';
  }
});

function showToast(msg, type = 'success') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `sk-toast ${type}`;
  toast.innerHTML = `<span class="material-symbols-outlined" style="font-size:18px">${type === 'success' ? 'check_circle' : type === 'error' ? 'error' : 'warning'}</span> ${msg}`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}
