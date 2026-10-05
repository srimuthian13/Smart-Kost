# SmartKost – Sistem Manajemen Kost-an | Jun 2026

**GitHub** : [https://github.com/srimuthian13/Smart-Kost](https://github.com/srimuthian13/Smart-Kost)  
**Tech Stack** : Java 21, Spring Boot, Spring Cloud, Spring Security (JWT), OpenFeign, Hibernate/JPA, PostgreSQL, Thymeleaf, JavaScript, Maven, Swagger

## 📝 Deskripsi Proyek
• Merancang dan mengembangkan aplikasi manajemen kost berbasis Spring Boot Microservices yang terdiri dari lima independent services yang terintegrasi melalui API Gateway dan Eureka Server.  
• Membangun RESTful API menggunakan Spring Boot, Spring Security (JWT), Hibernate/JPA, dan PostgreSQL untuk mengelola autentikasi, kamar, tenant, pembayaran, dan pengaduan.  
• Mengimplementasikan workflow pembayaran yang mencakup pembuatan invoice otomatis, upload bukti pembayaran, email notifikasi, scheduler tagihan bulanan, dan pembaruan status pembayaran.  
• Mengimplementasikan komunikasi antar-microservice menggunakan Spring Cloud OpenFeign serta dashboard berbasis Thymeleaf.  
• Mengembangkan fitur export laporan menggunakan Apache POI.  

## ⚙️ Cara Menjalankan Aplikasi

Karena aplikasi ini menggunakan arsitektur microservices, Anda perlu menjalankan beberapa service secara terpisah:

1. Clone repository ini:
   ```bash
   git clone https://github.com/srimuthian13/Smart-Kost.git
   cd Smart-Kost
   ```

2. Pastikan service database PostgreSQL Anda telah berjalan. Sesuaikan konfigurasi username dan password database pada file `application.properties` di masing-masing folder service.

3. Jalankan masing-masing service melalui IDE atau Maven wrapper di terminal terpisah.
