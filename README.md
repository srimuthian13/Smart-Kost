# Smart Kos Management System

Sistem Manajemen Kos Pintar (Smart Kos) berbasis arsitektur Microservices. Proyek ini mempermudah pengelolaan kamar kos, penyewa (tenant), pembayaran, serta keluhan (complaint).

## 🏗️ Arsitektur Sistem

Proyek ini dibangun menggunakan arsitektur **Microservices** dengan service-service berikut:

- **API Gateway (`api-gateway`)**: Gerbang utama untuk semua permintaan dari client ke microservices.
- **Auth Service (`auth_service`)**: Menangani otentikasi, otorisasi, dan manajemen pengguna.
- **Room Service (`room_service`)**: Mengelola data kamar kos, fasilitas, dan status ketersediaan.
- **Tenant Service (`tenant_service`)**: Mengelola data penyewa kos (tenant), status aktif, dan pendaftaran.
- **Payment Service (`payment_service`)**: Menangani pencatatan dan transaksi pembayaran sewa kos.
- **Complaint Service (`complaint_service`)**: Mengelola keluhan dan laporan masalah dari penyewa.
- **Smartkost Web (`smartkost`)**: Aplikasi Web (Frontend/Admin Dashboard) untuk antarmuka pengguna (Admin, Owner, dan Tenant).

## 🚀 Fitur Utama

- **Dashboard Pengguna**: Tampilan dashboard khusus dan terpisah untuk peran Admin, Owner, dan Tenant.
- **Manajemen Kamar**: Pembuatan kamar, pengaturan fasilitas, upload gambar kamar, dan pelacakan ketersediaan.
- **Manajemen Penyewa**: Pendaftaran penyewa baru beserta integrasinya dengan kamar.
- **Sistem Pembayaran Terintegrasi**: Simulasi QRIS, tracking status pembayaran (Lunas/Belum Lunas).
- **Sistem Komplain**: Penyewa dapat mengajukan keluhan, admin dapat memantau dan memproses keluhan tersebut.

## 🛠️ Teknologi yang Digunakan

- **Backend Framework**: Java, Spring Boot
- **Frontend Framework**: HTML, CSS, JavaScript (Thymeleaf)
- **Database**: MySQL / PostgreSQL *(silakan sesuaikan)*
- **Architecture Pattern**: Microservices (Spring Cloud Gateway, RestTemplate/FeignClient)

## ⚙️ Cara Menjalankan Aplikasi

Karena aplikasi ini menggunakan arsitektur microservices, Anda perlu menjalankan beberapa service secara terpisah:

1. Clone repository ini:
   ```bash
   git clone https://github.com/srimuthian13/Smart-Kost.git
   cd Smart-Kost
   ```

2. Pastikan service database Anda telah berjalan. Sesuaikan konfigurasi username dan password database pada file `application.properties` di masing-masing folder service.

3. Jalankan masing-masing service (Anda dapat membukanya di terminal terpisah dan menggunakan perintah maven wrapper):
   ```bash
   # Contoh untuk menjalankan room_service
   cd room_service
   ./mvnw spring-boot:run
   ```
   *Jalankan service lainnya (`auth_service`, `tenant_service`, `payment_service`, `complaint_service`, `api-gateway`, dan `smartkost`) dengan cara yang sama.*

4. Akses aplikasi frontend di browser Anda (biasanya di `http://localhost:8080` atau port yang di set di folder `smartkost`).

## 📂 Struktur Direktori

- `api-gateway/` - Routing request ke service-service di belakangnya.
- `auth_service/` - Layanan otentikasi JWT.
- `room_service/` - Layanan data dan logika kamar.
- `tenant_service/` - Layanan data dan logika penyewa.
- `payment_service/` - Layanan transaksi dan invoice.
- `complaint_service/` - Layanan manajemen komplain.
- `smartkost/` - Module untuk Frontend (HTML, CSS, JS, Controller Web).
- `uploads/` - Folder tempat menyimpan gambar/dokumen.
- `flowchart smartkos.drawio` - Flowchart alur sistem aplikasi.
