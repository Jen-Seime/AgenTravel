# Database Design - Sistem Informasi Agen Travel Bus

## Deskripsi Sistem

Sistem ini digunakan untuk pemesanan tiket bus secara online.

Terdapat 2 aktor utama:

1. Admin
2. User (Pelanggan)

Fitur utama:

* Login dan Registrasi User
* Kelola Data Bus
* Kelola Jadwal Keberangkatan
* Pemesanan Tiket
* Pemilihan Kursi
* Verifikasi Pembayaran
* Riwayat Pemesanan
* Cetak Tiket
* Laporan

---

# Entity Relationship

```text
Users
  │
  └──< Pemesanan >──── Jadwal
           │              │
           │              │
           ▼              ▼
   Detail_Pemesanan     Bus
           │
           ▼
         Kursi
```

---

# Tabel: admin

Menyimpan data administrator sistem.

| Field    | Type                  |
| -------- | --------------------- |
| id       | INT PK AUTO_INCREMENT |
| nama     | VARCHAR(100)          |
| username | VARCHAR(50)           |
| password | VARCHAR(255)          |

---

# Tabel: users

Menyimpan akun pelanggan.

| Field         | Type                  |
| ------------- | --------------------- |
| id            | INT PK AUTO_INCREMENT |
| nama          | VARCHAR(100)          |
| email         | VARCHAR(100)          |
| nomor_telepon | VARCHAR(15)           |
| password      | VARCHAR(255)          |

---

# Tabel: bus

Menyimpan data bus.

| Field        | Type                  |
| ------------ | --------------------- |
| id           | INT PK AUTO_INCREMENT |
| nama_bus     | VARCHAR(100)          |
| jumlah_kursi | INT                   |
| harga_tiket  | DECIMAL(10,2)         |
| fasilitas    | TEXT                  |

Contoh:

| id | nama_bus    | jumlah_kursi | harga_tiket |
| -- | ----------- | ------------ | ----------- |
| 1  | Executive A | 40           | 150000      |
| 2  | Executive B | 32           | 200000      |

---

# Tabel: jadwal

Menyimpan jadwal keberangkatan bus.

| Field             | Type                  |
| ----------------- | --------------------- |
| id                | INT PK AUTO_INCREMENT |
| bus_id            | INT FK                |
| asal              | VARCHAR(100)          |
| tujuan            | VARCHAR(100)          |
| tanggal_berangkat | DATE                  |
| jam_berangkat     | TIME                  |

Contoh:

| id | bus_id | asal     | tujuan |
| -- | ------ | -------- | ------ |
| 1  | 1      | Makassar | Mamuju |

---

# Tabel: kursi

Menyimpan daftar kursi untuk setiap jadwal.

| Field       | Type                       |
| ----------- | -------------------------- |
| id          | INT PK AUTO_INCREMENT      |
| jadwal_id   | INT FK                     |
| nomor_kursi | VARCHAR(10)                |
| status      | ENUM('Tersedia','Dipesan') |

Contoh:

| id | jadwal_id | nomor_kursi | status   |
| -- | --------- | ----------- | -------- |
| 1  | 1         | A1          | Dipesan  |
| 2  | 1         | A2          | Tersedia |
| 3  | 1         | A3          | Tersedia |

Catatan:

Saat admin membuat jadwal baru, sistem otomatis membuat kursi sesuai jumlah kursi bus.

Contoh Bus Executive A memiliki 40 kursi:

A1, A2, A3 ... A40

---

# Tabel: pemesanan

Menyimpan transaksi pemesanan tiket.

| Field             | Type                        |
| ----------------- | --------------------------- |
| id                | INT PK AUTO_INCREMENT       |
| kode_tiket        | VARCHAR(20)                 |
| user_id           | INT FK                      |
| jadwal_id         | INT FK                      |
| jumlah_tiket      | INT                         |
| total_bayar       | DECIMAL(10,2)               |
| metode_pembayaran | varcahr |
| created_at        | DATETIME                    |

Contoh:

| kode_tiket | user_id | jumlah_tiket |
| ---------- | ------- | ------------ |
| TKT001     | 1       | 2            |

---

# Tabel: detail_pemesanan

Menyimpan kursi yang dipilih pada setiap pemesanan.

| Field        | Type                  |
| ------------ | --------------------- |
| id           | INT PK AUTO_INCREMENT |
| pemesanan_id | INT FK                |
| kursi_id     | INT FK                |

Contoh:

Pemesanan TKT001:

* Kursi A1
* Kursi A2

---

# Flow Pemesanan

1. User login.
2. User melihat jadwal.
3. User memilih jadwal.
4. User memilih kursi.
5. Sistem membuat data pemesanan.
6. Status pembayaran = Belum Lunas.
7. Admin melakukan verifikasi pembayaran.
8. Status pembayaran = Lunas.
9. User dapat mengunduh tiket.

---

# Tiket

Informasi yang ditampilkan:

* Kode Tiket
* Nama User
* Nama Bus
* Kota Asal
* Kota Tujuan
* Tanggal Keberangkatan
* Jam Keberangkatan
* Nomor Kursi
* Total Pembayaran
* Status Pembayaran
