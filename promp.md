Buatkan fitur Kelola Bus untuk aplikasi Sistem Informasi Agen Travel Bus menggunakan Java dan MySQL.

Deskripsi:

Admin dapat mengelola data bus yang digunakan untuk keberangkatan. Data bus akan digunakan saat admin membuat jadwal keberangkatan.

Struktur tabel bus:

* id
* nama_bus
* jumlah_kursi
* harga_tiket
* fasilitas

Struktur tabel kursi:

* id
* jadwal_id
* nomor_kursi
* status

Keterangan:

Tabel kursi tidak dikelola langsung oleh admin. Data kursi akan dibuat otomatis saat admin membuat jadwal keberangkatan berdasarkan jumlah_kursi dari bus yang dipilih.

Contoh:

Bus Executive A

* jumlah_kursi = 40

Saat jadwal dibuat, sistem otomatis membuat:

* A1
* A2
* A3
* ...
* A40

Status awal:

* Tersedia

Fitur yang harus dibuat:

1. Tambah Bus

   * Admin dapat menambahkan data bus baru.
   * Mengisi:

     * Nama Bus
     * Jumlah Kursi
     * Harga Tiket
     * Fasilitas
   * Data disimpan ke database.

2. Lihat Data Bus

   * Menampilkan seluruh data bus dalam bentuk tabel.
   * Menampilkan:

     * Nama Bus
     * Jumlah Kursi
     * Harga Tiket
     * Fasilitas

3. Edit Bus

   * Admin dapat mengubah data bus.
   * Data lama ditampilkan pada form.
   * Setelah disimpan, data diperbarui di database.

4. Hapus Bus

   * Admin dapat menghapus data bus.
   * Sebelum menghapus tampilkan konfirmasi.
   * Jika bus masih digunakan pada jadwal keberangkatan, tampilkan pesan:
     "Bus tidak dapat dihapus karena masih digunakan pada jadwal."

Validasi:

* Nama bus wajib diisi.
* Jumlah kursi harus lebih dari 0.
* Harga tiket harus lebih dari 0.
* Fasilitas boleh kosong.

Alur Sistem:

Admin Login
↓
Menu Data Bus
↓
Tambah / Edit / Hapus Bus
↓
Simpan Perubahan
↓
Data Bus Tersimpan di Database
↓
Data Bus Digunakan Saat Pembuatan Jadwal
↓
Sistem Membuat Kursi Otomatis Berdasarkan jumlah_kursi

Gunakan Java, MySQL, JDBC, MVC Pattern, DAO Pattern, dan validasi form yang baik.
