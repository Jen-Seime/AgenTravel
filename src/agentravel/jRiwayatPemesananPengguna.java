/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package agentravel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author LENOVO
 */
public class jRiwayatPemesananPengguna extends javax.swing.JPanel {

    private int userId = -1;
    private String namaUser = "";

    /**
     * Creates new form jRiwayatPemesananPengguna
     */
    public jRiwayatPemesananPengguna() {
        this(-1, "");
    }

    public jRiwayatPemesananPengguna(int userId, String namaUser) {
        this.userId = userId;
        this.namaUser = namaUser;
        initComponents();
        setupForm();
        loadDataRiwayat();
        setupTableListener();
        btnDownloadTiket.addActionListener(evt -> downloadTiket());
    }

    public void setUserId(int userId, String namaUser) {
        this.userId = userId;
        this.namaUser = namaUser;
        setupForm();
        loadDataRiwayat();
        clearForm();
    }

    private void setupForm() {
        txtKodeTiket.setEditable(false);
        txtNama.setEditable(false);
        txtBus.setEditable(false);
        txtRute.setEditable(false);
        txtTanggal.setEditable(false);
        txtJam.setEditable(false);
        txtKursi.setEditable(false);
        txtJumlahTiket.setEditable(false);
        txtTotalBayar5.setEditable(false);
        txtStatus.setEditable(false);

        btnProfil.setText(namaUser == null || namaUser.trim().isEmpty() ? "PELANGGAN" : namaUser);
        btnDownloadTiket.setEnabled(false);
    }

    private void loadDataRiwayat() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Kode", "Nama", "Bus", "Rute", "Tanggal", "Jam", "Kursi", "Jumlah Tiket", "Total Bayar", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (userId <= 0) {
            tblRiwayatPemesanan.setModel(model);
            return;
        }

        String sql = "SELECT p.kode_tiket, u.nama AS nama_user, b.nama_bus, j.asal, j.tujuan, "
                   + "j.tanggal_berangkat, j.jam_berangkat, "
                   + "GROUP_CONCAT(k.nomor_kursi ORDER BY k.nomor_kursi SEPARATOR ', ') AS kursi, "
                   + "p.jumlah_tiket, p.total_bayar, p.status "
                   + "FROM pemesanan p "
                   + "JOIN users u ON p.user_id = u.id "
                   + "JOIN jadwal j ON p.jadwal_id = j.id "
                   + "JOIN bus b ON j.bus_id = b.id "
                   + "LEFT JOIN detail_pemesanan dp ON p.id = dp.pemesanan_id "
                   + "LEFT JOIN kursi k ON dp.kursi_id = k.id "
                   + "WHERE p.user_id = ? "
                   + "GROUP BY p.id, p.kode_tiket, u.nama, b.nama_bus, j.asal, j.tujuan, "
                   + "j.tanggal_berangkat, j.jam_berangkat, p.jumlah_tiket, p.total_bayar, p.status "
                   + "ORDER BY p.created_at DESC";

        try {
            Connection conn = AgenTravel.getKoneksi();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Koneksi database tidak tersedia!");
                tblRiwayatPemesanan.setModel(model);
                return;
            }

            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setInt(1, userId);
                try (ResultSet rs = pst.executeQuery()) {
                    NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
                    while (rs.next()) {
                        String rute = rs.getString("asal") + " - " + rs.getString("tujuan");
                        String status = rs.getString("status");
                        if (status == null || status.trim().isEmpty()) {
                            status = "Belum Terverifikasi";
                        }

                        model.addRow(new Object[]{
                            rs.getString("kode_tiket"),
                            rs.getString("nama_user"),
                            rs.getString("nama_bus"),
                            rute,
                            rs.getDate("tanggal_berangkat"),
                            rs.getTime("jam_berangkat"),
                            rs.getString("kursi") == null ? "-" : rs.getString("kursi"),
                            rs.getInt("jumlah_tiket"),
                            "Rp " + nf.format(rs.getDouble("total_bayar")),
                            status
                        });
                    }
                }
            }
            tblRiwayatPemesanan.setModel(model);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat riwayat pemesanan: " + ex.getMessage());
            tblRiwayatPemesanan.setModel(model);
        }
    }

    private void setupTableListener() {
        tblRiwayatPemesanan.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblRiwayatPemesanan.getSelectedRow();
                if (row >= 0) {
                    onRiwayatSelected(row);
                }
            }
        });
    }

    private void onRiwayatSelected(int row) {
        int modelRow = tblRiwayatPemesanan.convertRowIndexToModel(row);

        txtKodeTiket.setText(getTableValue(modelRow, 0));
        txtNama.setText(getTableValue(modelRow, 1));
        txtBus.setText(getTableValue(modelRow, 2));
        txtRute.setText(getTableValue(modelRow, 3));
        txtTanggal.setText(getTableValue(modelRow, 4));
        txtJam.setText(getTableValue(modelRow, 5));
        txtKursi.setText(getTableValue(modelRow, 6));
        txtJumlahTiket.setText(getTableValue(modelRow, 7));
        txtTotalBayar5.setText(getTableValue(modelRow, 8));
        txtStatus.setText(getTableValue(modelRow, 9));

        btnDownloadTiket.setEnabled("Terverifikasi".equalsIgnoreCase(txtStatus.getText().trim()));
    }

    private String getTableValue(int row, int column) {
        Object value = tblRiwayatPemesanan.getModel().getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private void clearForm() {
        txtKodeTiket.setText("");
        txtNama.setText("");
        txtBus.setText("");
        txtRute.setText("");
        txtTanggal.setText("");
        txtJam.setText("");
        txtKursi.setText("");
        txtJumlahTiket.setText("");
        txtTotalBayar5.setText("");
        txtStatus.setText("");
        btnDownloadTiket.setEnabled(false);
    }

    private void downloadTiket() {
        if (txtKodeTiket.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih riwayat pemesanan terlebih dahulu!");
            return;
        }

        if (!"Terverifikasi".equalsIgnoreCase(txtStatus.getText().trim())) {
            JOptionPane.showMessageDialog(this, "Tiket hanya bisa didapatkan jika status pembayaran sudah Terverifikasi.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Tiket");
        chooser.setSelectedFile(new File("Tiket-" + txtKodeTiket.getText().trim() + ".txt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("TIKET AGEN TRAVEL BUS");
            writer.println("=====================");
            writer.println("Kode Tiket      : " + txtKodeTiket.getText());
            writer.println("Nama            : " + txtNama.getText());
            writer.println("Bus             : " + txtBus.getText());
            writer.println("Rute            : " + txtRute.getText());
            writer.println("Tanggal         : " + txtTanggal.getText());
            writer.println("Jam             : " + txtJam.getText());
            writer.println("Kursi           : " + txtKursi.getText());
            writer.println("Jumlah Tiket    : " + txtJumlahTiket.getText());
            writer.println("Total Bayar     : " + txtTotalBayar5.getText());
            writer.println("Status          : " + txtStatus.getText());
            JOptionPane.showMessageDialog(this, "Tiket berhasil disimpan: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan tiket: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        txtNama = new javax.swing.JTextField();
        txtBus = new javax.swing.JTextField();
        txtStatus = new javax.swing.JTextField();
        txtKodeTiket = new javax.swing.JTextField();
        txtRute = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        txtJam = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        txtKursi = new javax.swing.JTextField();
        txtJumlahTiket = new javax.swing.JTextField();
        txtTanggal = new javax.swing.JTextField();
        txtTotalBayar5 = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        btnProfil = new javax.swing.JButton();
        btnDownloadTiket = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblRiwayatPemesanan = new javax.swing.JTable();

        jPanel2.setBackground(new java.awt.Color(230, 242, 221));

        jPanel3.setBackground(new java.awt.Color(136, 189, 164));

        jLabel1.setFont(new java.awt.Font("Perpetua Titling MT", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("RIWAYAT PEMESANAN");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(331, 331, 331)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel1)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(177, 211, 185));
        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Detail Pesananan", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua Titling MT", 1, 18), new java.awt.Color(101, 146, 135))); // NOI18N
        jPanel4.setPreferredSize(new java.awt.Dimension(653, 208));

        txtStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtStatusActionPerformed(evt);
            }
        });

        txtKodeTiket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKodeTiketActionPerformed(evt);
            }
        });

        txtRute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRuteActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Kode Tiket");

        jLabel8.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Nama");

        jLabel9.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Tanggal");

        jLabel10.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("RUTE");

        jLabel11.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("BUS");

        jLabel13.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(255, 255, 255));
        jLabel13.setText("TOTAL BAYAR");

        txtJam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtJamActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("jam");

        jLabel15.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(255, 255, 255));
        jLabel15.setText("kursi");

        jLabel16.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("jumlah tiket");

        txtKursi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtKursiActionPerformed(evt);
            }
        });

        txtJumlahTiket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtJumlahTiketActionPerformed(evt);
            }
        });

        txtTanggal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTanggalActionPerformed(evt);
            }
        });

        txtTotalBayar5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtTotalBayar5ActionPerformed(evt);
            }
        });

        jLabel17.setFont(new java.awt.Font("Perpetua Titling MT", 1, 14)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("status");

        btnProfil.setBackground(new java.awt.Color(102, 153, 255));
        btnProfil.setFont(new java.awt.Font("Perpetua Titling MT", 0, 14)); // NOI18N
        btnProfil.setForeground(new java.awt.Color(255, 255, 255));
        btnProfil.setText("PELANGGAN");

        btnDownloadTiket.setBackground(new java.awt.Color(51, 102, 255));
        btnDownloadTiket.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        btnDownloadTiket.setForeground(new java.awt.Color(255, 255, 255));
        btnDownloadTiket.setText("Download Tiket");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel8)
                                        .addComponent(jLabel10))
                                    .addGap(203, 203, 203))
                                .addGroup(jPanel4Layout.createSequentialGroup()
                                    .addComponent(txtKodeTiket)
                                    .addGap(25, 25, 25)))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(txtRute, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(25, 25, 25)))
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel16)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(txtJumlahTiket, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnDownloadTiket)
                                .addGap(51, 51, 51))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel15)
                                    .addComponent(jLabel9)
                                    .addComponent(jLabel14)
                                    .addComponent(txtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel17)
                                            .addComponent(jLabel13))
                                        .addGap(159, 159, 159))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                        .addComponent(btnProfil, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(78, 78, 78))))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(txtJam, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtTotalBayar5, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(txtBus, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addComponent(txtKursi, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtKodeTiket, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(btnProfil, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(13, 13, 13)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtJam, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtTotalBayar5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(71, 71, 71)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtRute, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtJumlahTiket, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel15)
                            .addComponent(jLabel17))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtKursi, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addComponent(btnDownloadTiket)
                        .addGap(24, 24, 24))))
        );

        tblRiwayatPemesanan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Kode", "Nama", "Bus", "Rute", "Tanggal", "Jam", "Kursi", "Jumlah Tiket", "Total Bayar", "Status"
            }
        ));
        tblRiwayatPemesanan.setPreferredSize(new java.awt.Dimension(653, 208));
        jScrollPane1.setViewportView(tblRiwayatPemesanan);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 857, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 351, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtStatusActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtStatusActionPerformed

    private void txtKodeTiketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKodeTiketActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKodeTiketActionPerformed

    private void txtRuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRuteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtRuteActionPerformed

    private void txtJamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJamActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtJamActionPerformed

    private void txtKursiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtKursiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtKursiActionPerformed

    private void txtJumlahTiketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtJumlahTiketActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtJumlahTiketActionPerformed

    private void txtTanggalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTanggalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTanggalActionPerformed

    private void txtTotalBayar5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTotalBayar5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtTotalBayar5ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDownloadTiket;
    private javax.swing.JButton btnProfil;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblRiwayatPemesanan;
    private javax.swing.JTextField txtBus;
    private javax.swing.JTextField txtJam;
    private javax.swing.JTextField txtJumlahTiket;
    private javax.swing.JTextField txtKodeTiket;
    private javax.swing.JTextField txtKursi;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtRute;
    private javax.swing.JTextField txtStatus;
    private javax.swing.JTextField txtTanggal;
    private javax.swing.JTextField txtTotalBayar5;
    // End of variables declaration//GEN-END:variables
}
