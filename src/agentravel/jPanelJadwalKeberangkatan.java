/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package agentravel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ASUS
 */
public class jPanelJadwalKeberangkatan extends javax.swing.JPanel {

    private int selectedJadwalId = -1;
    // Array untuk menyimpan bus_id yang sesuai dengan index ComboBox
    private java.util.List<Integer> busIdList = new java.util.ArrayList<>();

    /**
     * Creates new form jPanelJadwalKeberangkatan
     */
    public jPanelJadwalKeberangkatan() {
        initComponents();
        configureSpinners();
        loadBusComboBox();
        loadDataJadwal();
        setupTableListener();
    }

    // ==================== CONFIGURE SPINNERS ====================
    private void configureSpinners() {
        jDateChooser1.setDate(new java.util.Date());
        jTextField3.setText("12:00");
    }

    // ==================== LOAD BUS KE COMBOBOX ====================
    private void loadBusComboBox() {
        jComboBox1.removeAllItems();
        busIdList.clear();

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "SELECT id, nama_bus FROM bus ORDER BY nama_bus ASC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                busIdList.add(rs.getInt("id"));
                jComboBox1.addItem(rs.getString("nama_bus"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data bus: " + e.getMessage());
        }
    }

    // ==================== LOAD DATA JADWAL KE TABEL ====================
    private void loadDataJadwal() {
        DefaultTableModel model = new DefaultTableModel(
                new String[] { "ID", "BUS ID", "BUS", "ASAL", "TUJUAN", "TANGGAL", "JAM", "SISA KURSI" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "SELECT j.id, j.bus_id, b.nama_bus, j.asal, j.tujuan, j.tanggal_berangkat, j.jam_berangkat, "
                    + "(SELECT COUNT(*) FROM kursi k WHERE k.jadwal_id = j.id AND k.status = 'Tersedia') AS sisa_kursi "
                    + "FROM jadwal j JOIN bus b ON j.bus_id = b.id ORDER BY j.tanggal_berangkat ASC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[] {
                        rs.getInt("id"),
                        rs.getInt("bus_id"),
                        rs.getString("nama_bus"),
                        rs.getString("asal"),
                        rs.getString("tujuan"),
                        rs.getString("tanggal_berangkat"),
                        rs.getString("jam_berangkat"),
                        rs.getInt("sisa_kursi")
                });
            }

            jTable1.setModel(model);
            sembunyikanKolomId();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data jadwal: " + e.getMessage());
        }
    }

    // ==================== KLIK TABEL UNTUK MENGISI FORM ====================
    private void setupTableListener() {
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.getSelectedRow();
                if (row >= 0) {
                    isiFormDariTabel(row);
                }
            }
        });
    }

    private void isiFormDariTabel(int row) {
        int modelRow = jTable1.convertRowIndexToModel(row);
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();

        selectedJadwalId = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        int busId = Integer.parseInt(model.getValueAt(modelRow, 1).toString());

        for (int i = 0; i < busIdList.size(); i++) {
            if (busIdList.get(i) == busId) {
                jComboBox1.setSelectedIndex(i);
                break;
            }
        }

        jTextField1.setText(getTableValue(modelRow, 3));
        jTextField2.setText(getTableValue(modelRow, 4));

        try {
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
            jDateChooser1.setDate(sdfDate.parse(getTableValue(modelRow, 5)));
        } catch (Exception e) {
            jDateChooser1.setDate(new java.util.Date());
        }

        String jamBerangkat = getTableValue(modelRow, 6);
        if (jamBerangkat.length() > 5) {
            jTextField3.setText(jamBerangkat.substring(0, 5));
        } else if (!jamBerangkat.isEmpty()) {
            jTextField3.setText(jamBerangkat);
        } else {
            jTextField3.setText("12:00");
        }
    }

    private String getTableValue(int row, int column) {
        Object value = jTable1.getModel().getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private void sembunyikanKolomId() {
        if (jTable1.getColumnCount() > 1) {
            for (int i = 0; i <= 1; i++) {
                jTable1.getColumnModel().getColumn(i).setMinWidth(0);
                jTable1.getColumnModel().getColumn(i).setMaxWidth(0);
                jTable1.getColumnModel().getColumn(i).setWidth(0);
            }
        }
    }

    // ==================== VALIDASI FORM ====================
    private boolean validasiForm() {
        if (jComboBox1.getItemCount() == 0) {
            JOptionPane.showMessageDialog(this, "Belum ada data bus! Tambahkan bus terlebih dahulu.");
            return false;
        }

        if (jTextField1.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kota asal wajib diisi!");
            jTextField1.requestFocus();
            return false;
        }

        if (jTextField2.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kota tujuan wajib diisi!");
            jTextField2.requestFocus();
            return false;
        }

        if (jDateChooser1.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Tanggal berangkat wajib diisi!");
            return false;
        }

        String jamInput = jTextField3.getText().trim();
        if (jamInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jam berangkat wajib diisi!");
            jTextField3.requestFocus();
            return false;
        }

        if (!jamInput.matches("^\\d{2}:\\d{2}(:\\d{2})?$")) {
            JOptionPane.showMessageDialog(this, "Format jam salah! Gunakan format HH:mm (contoh: 14:30)");
            jTextField3.requestFocus();
            return false;
        }

        return true;
    }

    // ==================== BERSIHKAN FORM ====================
    private void clearForm() {
        if (jComboBox1.getItemCount() > 0) {
            jComboBox1.setSelectedIndex(0);
        }
        jTextField1.setText("");
        jTextField2.setText("");
        jDateChooser1.setDate(new java.util.Date());
        jTextField3.setText("12:00");
        selectedJadwalId = -1;
        jTable1.clearSelection();
    }

    // ==================== SIMPAN JADWAL + AUTO CREATE KURSI ====================
    private void simpanJadwal() {
        if (!validasiForm())
            return;

        int busId = busIdList.get(jComboBox1.getSelectedIndex());
        String asal = jTextField1.getText().trim();
        String tujuan = jTextField2.getText().trim();

        java.util.Date tanggalDate = jDateChooser1.getDate();
        java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String tanggal = sdfDate.format(tanggalDate);

        String jam = jTextField3.getText().trim();

        Connection conn = null;
        try {
            conn = AgenTravel.getKoneksi();
            conn.setAutoCommit(false); // mulai transaction

            // 1. Simpan jadwal dan ambil ID yang di-generate
            String sqlJadwal = "INSERT INTO jadwal (bus_id, asal, tujuan, tanggal_berangkat, jam_berangkat) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstJadwal = conn.prepareStatement(sqlJadwal, Statement.RETURN_GENERATED_KEYS);
            pstJadwal.setInt(1, busId);
            pstJadwal.setString(2, asal);
            pstJadwal.setString(3, tujuan);
            pstJadwal.setString(4, tanggal);
            pstJadwal.setString(5, jam);
            pstJadwal.executeUpdate();

            // Ambil ID jadwal yang baru dibuat
            ResultSet generatedKeys = pstJadwal.getGeneratedKeys();
            int jadwalId = -1;
            if (generatedKeys.next()) {
                jadwalId = generatedKeys.getInt(1);
            }

            // 2. Ambil jumlah_kursi dari bus yang dipilih
            String sqlBus = "SELECT jumlah_kursi FROM bus WHERE id = ?";
            PreparedStatement pstBus = conn.prepareStatement(sqlBus);
            pstBus.setInt(1, busId);
            ResultSet rsBus = pstBus.executeQuery();

            int jumlahKursi = 0;
            if (rsBus.next()) {
                jumlahKursi = rsBus.getInt("jumlah_kursi");
            }

            // 3. Auto-create kursi: A1, A2, A3, ... A(jumlah_kursi)
            String sqlKursi = "INSERT INTO kursi (jadwal_id, nomor_kursi, status) VALUES (?, ?, 'Tersedia')";
            PreparedStatement pstKursi = conn.prepareStatement(sqlKursi);

            for (int i = 1; i <= jumlahKursi; i++) {
                String nomorKursi = "A" + i;
                pstKursi.setInt(1, jadwalId);
                pstKursi.setString(2, nomorKursi);
                pstKursi.addBatch();
            }
            pstKursi.executeBatch();

            conn.commit(); // commit transaction

            JOptionPane.showMessageDialog(this,
                    "Jadwal berhasil disimpan!\n" + jumlahKursi + " kursi otomatis dibuat (A1 - A" + jumlahKursi + ")");
            clearForm();
            loadDataJadwal();

        } catch (Exception e) {
            // Rollback jika ada error
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            JOptionPane.showMessageDialog(this, "Gagal menyimpan jadwal: " + e.getMessage());
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }

    // ==================== EDIT JADWAL ====================
    private void editJadwal() {
        if (selectedJadwalId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin diedit pada tabel!");
            return;
        }

        if (!validasiForm())
            return;

        int busId = busIdList.get(jComboBox1.getSelectedIndex());
        String asal = jTextField1.getText().trim();
        String tujuan = jTextField2.getText().trim();

        java.util.Date tanggalDate = jDateChooser1.getDate();
        java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
        String tanggal = sdfDate.format(tanggalDate);

        String jam = jTextField3.getText().trim();

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "UPDATE jadwal SET bus_id = ?, asal = ?, tujuan = ?, tanggal_berangkat = ?, jam_berangkat = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, busId);
            pst.setString(2, asal);
            pst.setString(3, tujuan);
            pst.setString(4, tanggal);
            pst.setString(5, jam);
            pst.setInt(6, selectedJadwalId);

            int result = pst.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Jadwal berhasil diperbarui!");
                clearForm();
                loadDataJadwal();
            } else {
                JOptionPane.showMessageDialog(this, "Jadwal tidak ditemukan atau sudah dihapus.");
                clearForm();
                loadDataJadwal();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui jadwal: " + e.getMessage());
        }
    }

    // ==================== HAPUS JADWAL + KURSI ====================
    private void hapusJadwal() {
        if (selectedJadwalId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal yang ingin dihapus pada tabel!");
            return;
        }

        // Cek apakah ada pemesanan pada jadwal ini
        try {
            Connection conn = AgenTravel.getKoneksi();
            String cekSql = "SELECT COUNT(*) AS total FROM pemesanan WHERE jadwal_id = ?";
            PreparedStatement pstCek = conn.prepareStatement(cekSql);
            pstCek.setInt(1, selectedJadwalId);
            ResultSet rsCek = pstCek.executeQuery();

            if (rsCek.next() && rsCek.getInt("total") > 0) {
                JOptionPane.showMessageDialog(this,
                        "Jadwal tidak dapat dihapus karena sudah ada pemesanan tiket.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memeriksa pemesanan: " + e.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus jadwal ini?\nSemua data kursi pada jadwal ini juga akan dihapus.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        Connection conn = null;
        try {
            conn = AgenTravel.getKoneksi();
            conn.setAutoCommit(false);

            // Hapus kursi dulu (child)
            String sqlKursi = "DELETE FROM kursi WHERE jadwal_id = ?";
            PreparedStatement pstKursi = conn.prepareStatement(sqlKursi);
            pstKursi.setInt(1, selectedJadwalId);
            pstKursi.executeUpdate();

            // Hapus jadwal (parent)
            String sqlJadwal = "DELETE FROM jadwal WHERE id = ?";
            PreparedStatement pstJadwal = conn.prepareStatement(sqlJadwal);
            pstJadwal.setInt(1, selectedJadwalId);
            int deletedJadwal = pstJadwal.executeUpdate();

            if (deletedJadwal == 0) {
                conn.rollback();
                JOptionPane.showMessageDialog(this, "Jadwal tidak ditemukan atau sudah dihapus.");
                clearForm();
                loadDataJadwal();
                return;
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, "Jadwal dan data kursi berhasil dihapus!");
            clearForm();
            loadDataJadwal();

        } catch (Exception e) {
            try {
                if (conn != null)
                    conn.rollback();
            } catch (Exception ex) {
            }
            JOptionPane.showMessageDialog(this, "Gagal menghapus jadwal: " + e.getMessage());
        } finally {
            try {
                if (conn != null)
                    conn.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jTextField3 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jPanel2.setBackground(new java.awt.Color(101, 146, 135));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 67, Short.MAX_VALUE)
        );

        jPanel3.setBackground(new java.awt.Color(230, 242, 221));

        jPanel4.setBackground(new java.awt.Color(136, 189, 164));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Form Data Bus", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua Titling MT", 0, 12), new java.awt.Color(255, 255, 255))); // NOI18N

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jLabel1.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Bus");

        jLabel2.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("asal");

        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jLabel3.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("tujuan");

        jLabel4.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Jam");

        jLabel5.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("TANGGAL");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1)
                    .addComponent(jComboBox1, 0, 196, Short.MAX_VALUE))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 225, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(135, 135, 135)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(173, 173, 173)
                                .addComponent(jLabel5))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 19, Short.MAX_VALUE)))
                .addGap(34, 34, 34))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(30, 30, 30)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jDateChooser1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(62, Short.MAX_VALUE))
        );

        jButton1.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jButton1.setText("SIMPAN");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jButton2.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jButton2.setText("EDIT");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton3.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jButton3.setText("Hapus");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "BUS", "ASAL", "TUJUAN", "TANGGAL", "JAM", "SISA KURSI"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 187, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(14, 14, 14))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton1ActionPerformed
        simpanJadwal();
    }// GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton2ActionPerformed
        editJadwal();
    }// GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jButton3ActionPerformed
        hapusJadwal();
    }// GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jComboBox1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }// GEN-LAST:event_jTextField2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    // End of variables declaration//GEN-END:variables
}
