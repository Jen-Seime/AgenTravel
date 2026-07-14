# 🚌 AgenTravel — Sistem Informasi Agen Travel Bus

Aplikasi desktop berbasis Java Swing untuk manajemen pemesanan tiket bus secara online. Sistem ini dirancang untuk memudahkan pengelolaan jadwal keberangkatan, pemesanan kursi, verifikasi pembayaran, dan pelaporan bagi agen travel bus.

---

## 📋 Deskripsi Proyek

**AgenTravel** adalah sistem informasi agen travel bus yang memiliki dua peran utama:

| Peran | Akses |
|-------|-------|
| 👨‍💼 **Admin** | Kelola data bus, jadwal, pembayaran, dan laporan |
| 👤 **Pengguna (Pelanggan)** | Daftar akun, lihat jadwal, pesan tiket, pilih kursi, cetak tiket |

---

## ✨ Fitur Utama

### 🔐 Autentikasi
- Login & Registrasi Pengguna
- Login Admin

### 👨‍💼 Panel Admin
- **Kelola Data Bus** — Tambah, edit, hapus data bus beserta fasilitas dan harga tiket
- **Kelola Jadwal Keberangkatan** — Atur jadwal bus per rute dan tanggal (kursi dibuat otomatis)
- **Kelola Pembayaran** — Verifikasi pembayaran pelanggan (ubah status menjadi *Lunas*)
- **Laporan Pemesanan** — Lihat laporan seluruh transaksi tiket

### 👤 Panel Pelanggan
- **Lihat Jadwal Bus** — Cari jadwal berdasarkan rute dan tanggal
- **Pemesanan Tiket** — Pilih jadwal, pilih kursi secara visual, buat pesanan
- **Riwayat Pemesanan** — Lihat histori semua pemesanan
- **Cetak Tiket** — Unduh/cetak tiket setelah pembayaran terverifikasi

---

## 🛠️ Teknologi yang Digunakan

| Komponen | Teknologi |
|----------|-----------|
| Bahasa Pemrograman | Java 25 |
| GUI Framework | Java Swing (NetBeans GUI Builder) |
| Database | MySQL |
| JDBC Driver | MySQL Connector/J 9.7.0 |
| Date Picker | JCalendar 1.4 |
| IDE | Apache NetBeans |
| Build Tool | Apache Ant (`build.xml`) |

---

## 🗄️ Struktur Database

Database: `travel_bus_db`

```
users
  │
  └──< pemesanan >──── jadwal
           │                │
           ▼                ▼
   detail_pemesanan        bus
           │
           ▼
         kursi
```

### Tabel Utama

| Tabel | Deskripsi |
|-------|-----------|
| `admin` | Data administrator sistem |
| `users` | Akun pelanggan |
| `bus` | Data armada bus (nama, kapasitas, harga, fasilitas) |
| `jadwal` | Jadwal keberangkatan bus per rute |
| `kursi` | Daftar kursi tiap jadwal (otomatis dibuat saat jadwal dibuat) |
| `pemesanan` | Transaksi pemesanan tiket pelanggan |
| `detail_pemesanan` | Detail kursi yang dipilih per pemesanan |

---

## 📦 Prasyarat Instalasi

Pastikan sudah menginstal semua komponen berikut:

- ✅ **JDK 25** atau lebih baru — [Download JDK](https://www.oracle.com/java/technologies/downloads/)
- ✅ **Apache NetBeans** (disarankan versi 21+) — [Download NetBeans](https://netbeans.apache.org/front/main/download/)
- ✅ **MySQL Server** (versi 8.0+) — [Download MySQL](https://dev.mysql.com/downloads/mysql/)
- ✅ **MySQL Connector/J 9.7.0** — [Download](https://dev.mysql.com/downloads/connector/j/)
- ✅ **JCalendar 1.4** — [Download](https://toedter.com/jcalendar/)

---

## ⚙️ Instalasi & Konfigurasi

### 1. Clone / Download Proyek

```bash
git clone https://github.com/Jen-Seime/AgenTravel.git
```

Atau download ZIP dan ekstrak ke folder pilihan Anda.

---

### 2. Buat Database MySQL

Buka MySQL CLI atau aplikasi seperti **phpMyAdmin** / **HeidiSQL**, lalu jalankan perintah berikut:

```sql
CREATE DATABASE travel_bus_db;
USE travel_bus_db;

-- Tabel admin
CREATE TABLE admin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nama VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- Tabel users
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nama VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    nomor_telepon VARCHAR(15),
    password VARCHAR(255) NOT NULL
);

-- Tabel bus
CREATE TABLE bus (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nama_bus VARCHAR(100) NOT NULL,
    jumlah_kursi INT NOT NULL,
    harga_tiket DECIMAL(10,2) NOT NULL,
    fasilitas TEXT
);

-- Tabel jadwal
CREATE TABLE jadwal (
    id INT PRIMARY KEY AUTO_INCREMENT,
    bus_id INT NOT NULL,
    asal VARCHAR(100) NOT NULL,
    tujuan VARCHAR(100) NOT NULL,
    tanggal_berangkat DATE NOT NULL,
    jam_berangkat TIME NOT NULL,
    FOREIGN KEY (bus_id) REFERENCES bus(id)
);

-- Tabel kursi
CREATE TABLE kursi (
    id INT PRIMARY KEY AUTO_INCREMENT,
    jadwal_id INT NOT NULL,
    nomor_kursi VARCHAR(10) NOT NULL,
    status ENUM('Tersedia', 'Dipesan') DEFAULT 'Tersedia',
    FOREIGN KEY (jadwal_id) REFERENCES jadwal(id)
);

-- Tabel pemesanan
CREATE TABLE pemesanan (
    id INT PRIMARY KEY AUTO_INCREMENT,
    kode_tiket VARCHAR(20) UNIQUE NOT NULL,
    user_id INT NOT NULL,
    jadwal_id INT NOT NULL,
    jumlah_tiket INT NOT NULL,
    total_bayar DECIMAL(10,2) NOT NULL,
    metode_pembayaran VARCHAR(50),
    status_pembayaran ENUM('Belum Lunas', 'Lunas') DEFAULT 'Belum Lunas',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (jadwal_id) REFERENCES jadwal(id)
);

-- Tabel detail_pemesanan
CREATE TABLE detail_pemesanan (
    id INT PRIMARY KEY AUTO_INCREMENT,
    pemesanan_id INT NOT NULL,
    kursi_id INT NOT NULL,
    FOREIGN KEY (pemesanan_id) REFERENCES pemesanan(id),
    FOREIGN KEY (kursi_id) REFERENCES kursi(id)
);

-- Insert akun admin default
INSERT INTO admin (nama, username, password) VALUES ('Administrator', 'admin', 'admin123');
```

---

### 3. Konfigurasi Koneksi Database

Buka file `src/agentravel/AgenTravel.java` dan sesuaikan konfigurasi koneksi:

```java
String url      = "jdbc:mysql://localhost:3306/travel_bus_db";
String user     = "root";       // ← ganti dengan username MySQL Anda
String password = "";           // ← ganti dengan password MySQL Anda
```

---

### 4. Tambahkan Library ke Proyek NetBeans

1. Buka proyek di **Apache NetBeans**
2. Klik kanan pada proyek → **Properties** → **Libraries**
3. Klik **Add JAR/Folder** dan tambahkan:
   - `mysql-connector-j-9.7.0.jar`
   - `jcalendar-1.4.jar`

> **Lokasi default library** (sesuai konfigurasi proyek):
> - `C:\Users\ASUS\backend\java\mysql-connector-j-9.7.0\...\mysql-connector-j-9.7.0.jar`
> - `C:\Users\ASUS\backend\java\jcalendar-1.4\lib\jcalendar-1.4.jar`

---

### 5. Jalankan Aplikasi

1. Pastikan **MySQL Server** sudah berjalan
2. Di NetBeans, klik kanan pada proyek → **Run Project** (atau tekan `F6`)
3. Aplikasi akan terbuka dengan form **Login Admin**

---

## 🔐 Akun Default

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `admin123` |

> ⚠️ Disarankan untuk mengganti password admin setelah pertama kali login.

---

## 📁 Struktur Proyek

```
AgenTravel/
├── src/
│   └── agentravel/
│       ├── AgenTravel.java                          # Entry point & koneksi database
│       ├── JLoginAdmin.java                         # Form login admin
│       ├── JLoginPengguna.java                      # Form login pengguna
│       ├── jDaftarAkunPengguna.java                 # Registrasi pengguna baru
│       ├── dashboardAdmin.java                      # Dashboard utama admin
│       ├── dashboardPelanggan.java                  # Dashboard utama pelanggan
│       ├── jPanelKelolaBus.java                     # Manajemen data bus
│       ├── jPanelJadwalKeberangkatan.java           # Manajemen jadwal
│       ├── jKelolaPembayaranAdmin.java              # Verifikasi pembayaran
│       ├── jPanelLaporanPemesananTiketAdmin.java    # Laporan transaksi
│       ├── jJadwalBusPengguna.java                  # Lihat jadwal & pesan tiket
│       └── jRiwayatPemesananPengguna.java           # Riwayat pemesanan
├── nbproject/                                       # Konfigurasi NetBeans
├── build.xml                                        # Build script Apache Ant
├── db-travel.md                                     # Dokumentasi desain database
└── README.md                                        # Dokumentasi proyek ini
```

---

## 🔄 Alur Pemesanan Tiket

```
1. Pengguna login / daftar akun
        ↓
2. Lihat daftar jadwal keberangkatan
        ↓
3. Pilih jadwal yang diinginkan
        ↓
4. Pilih kursi yang tersedia
        ↓
5. Konfirmasi & buat pemesanan
        ↓
6. Status: "Belum Lunas" → lakukan pembayaran
        ↓
7. Admin memverifikasi pembayaran
        ↓
8. Status: "Lunas" → Pengguna dapat cetak tiket
```

---

## 📄 Lisensi

Proyek ini dibuat untuk keperluan akademik / edukasi.

---

> Dibuat dengan  menggunakan Java Swing & MySQL
