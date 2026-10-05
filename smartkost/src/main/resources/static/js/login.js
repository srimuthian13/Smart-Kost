// ---- TAB SWITCH ----
function switchTab(tab) {
  document.getElementById('panelLogin').classList.toggle('active', tab === 'login');
  document.getElementById('panelRegister').classList.toggle('active', tab === 'register');
  document.getElementById('tabLogin').classList.toggle('active', tab === 'login');
  document.getElementById('tabRegister').classList.toggle('active', tab === 'register');
  clearAlerts();
}

function clearAlerts() {
  ['loginError','loginSuccess','regError','regSuccess'].forEach(id => {
    document.getElementById(id)?.classList.add('hidden');
  });
}

// ---- TOGGLE PASSWORD ----
function togglePassword(inputId, btnId) {
  const input = document.getElementById(inputId);
  const btn   = document.getElementById(btnId);
  const isPass = input.type === 'password';
  input.type = isPass ? 'text' : 'password';
  btn.textContent = isPass ? 'visibility_off' : 'visibility';
}

// ---- SET LOADING ----
function setLoading(btnId, loaderId, textId, loading) {
  const btn  = document.getElementById(btnId);
  const ldr  = document.getElementById(loaderId);
  const txt  = document.getElementById(textId);
  btn.disabled = loading;
  ldr.style.display = loading ? 'block' : 'none';
  txt.style.opacity = loading ? '0' : '1';
}

// ---- SHOW ALERT ----
function showAlert(id, msgId, message, type) {
  const el = document.getElementById(id);
  if (msgId) document.getElementById(msgId).textContent = message;
  el.className = `alert alert-${type}`;
  el.classList.remove('hidden');
  setTimeout(() => el.classList.add('hidden'), 6000);
}

document.addEventListener('DOMContentLoaded', () => {
  if (window.location.pathname.endsWith('/register')) {
    switchTab('register');
  }
});

// ---- LOGIN ----
document.getElementById('loginForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearAlerts();

  const email    = document.getElementById('loginEmail').value.trim();
  const password = document.getElementById('loginPassword').value;
  const remember = document.getElementById('rememberMe').checked;

  if (!email || !password) return;

  setLoading('loginBtn', 'loginLoader', 'loginBtnText', true);

  try {
    const res = await fetch('http://localhost:8080/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password, remember })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Email atau password salah. Silakan coba lagi.');
    }

    const data = await res.json();

    // Simpan data sesi
    sessionStorage.setItem('accessToken',  data.accessToken  || '');
    sessionStorage.setItem('refreshToken', data.refreshToken || '');
    sessionStorage.setItem('userId',       data.user?.id     || '');
    sessionStorage.setItem('username',     data.user?.username || email.split('@')[0]);
    sessionStorage.setItem('role',         data.user?.role   || '');
    sessionStorage.setItem('email',        data.user?.email  || email);

    if (remember) {
      localStorage.setItem('rememberedEmail', email);
    }

    showAlert('loginSuccess', 'loginSuccessMsg', 'Login berhasil! Mengalihkan...', 'success');

    const role = data.user?.role || '';
    setTimeout(() => {
      const pendingRoomId = localStorage.getItem('pendingRentRoomId');
      if (role === 'TENANT' && pendingRoomId) {
        localStorage.removeItem('pendingRentRoomId');
        localStorage.setItem('triggerRentRoomId', pendingRoomId);
        window.location.href = '/';
      } else if (role === 'ADMIN')       window.location.href = '/dashboard/admin';
      else if (role === 'TENANT') window.location.href = '/';
      else                        window.location.href = '/dashboard';
    }, 900);

  } catch (err) {
    showAlert('loginError', 'loginErrorMsg', err.message, 'danger');
  } finally {
    setLoading('loginBtn', 'loginLoader', 'loginBtnText', false);
  }
});

// ---- REGISTER ----
document.getElementById('registerForm').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearAlerts();

  const username = document.getElementById('regUsername').value.trim();
  const email    = document.getElementById('regEmail').value.trim();
  const phone    = document.getElementById('regPhone').value.trim();
  const password = document.getElementById('regPassword').value;
  const confirm  = document.getElementById('regConfirm').value;

  if (!username || !email || !password) {
    showAlert('regError', 'regErrorMsg', 'Harap isi semua kolom yang wajib diisi.', 'danger');
    return;
  }
  if (password.length < 8) {
    showAlert('regError', 'regErrorMsg', 'Password minimal 8 karakter.', 'danger');
    return;
  }
  if (password !== confirm) {
    showAlert('regError', 'regErrorMsg', 'Konfirmasi password tidak sesuai.', 'danger');
    return;
  }

  setLoading('regBtn', 'regLoader', 'regBtnText', true);

  try {
    const res = await fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, email, phone, password })
    });

    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || 'Pendaftaran gagal. Email mungkin sudah digunakan.');
    }
    const data = await res.json();

    if (data.accessToken) {
      sessionStorage.setItem('accessToken',  data.accessToken  || '');
      sessionStorage.setItem('refreshToken', data.refreshToken || '');
      sessionStorage.setItem('userId',       data.user?.id     || '');
      sessionStorage.setItem('username',     data.user?.username || email.split('@')[0]);
      sessionStorage.setItem('role',         data.user?.role   || '');
      sessionStorage.setItem('email',        data.user?.email  || email);
      
      document.getElementById('regSuccess').classList.remove('hidden');
      document.getElementById('registerForm').reset();
      
      setTimeout(() => {
        const role = data.user?.role || '';
        const pendingRoomId = localStorage.getItem('pendingRentRoomId');
        if (role === 'TENANT' && pendingRoomId) {
          localStorage.removeItem('pendingRentRoomId');
          localStorage.setItem('triggerRentRoomId', pendingRoomId);
          window.location.href = '/';
        } else if (role === 'ADMIN')       window.location.href = '/dashboard/admin';
        else if (role === 'TENANT') window.location.href = '/';
        else                        window.location.href = '/dashboard';
      }, 1000);
    } else {
      document.getElementById('regSuccess').classList.remove('hidden');
      document.getElementById('registerForm').reset();
      setTimeout(() => switchTab('login'), 2000);
    }

  } catch (err) {
    showAlert('regError', 'regErrorMsg', err.message, 'danger');
  } finally {
    setLoading('regBtn', 'regLoader', 'regBtnText', false);
  }
});

// Auto-fill remembered email
const rememberedEmail = localStorage.getItem('rememberedEmail');
if (rememberedEmail) {
  document.getElementById('loginEmail').value = rememberedEmail;
  document.getElementById('rememberMe').checked = true;
}
