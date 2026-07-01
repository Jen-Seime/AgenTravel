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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ACER NITRO V15
 */
public class jPanelLaporanPemesananTiketAdmin extends javax.swing.JPanel {

    /**
     * Creates new form jPanelLaporanPemesananTiketAdmin
     */
    public jPanelLaporanPemesananTiketAdmin() {
        initComponents();
        setupForm();
        loadLaporanPemesanan();
    }

    private void setupForm() {
        jTextField1.setEditable(false);
    }

    private void loadLaporanPemesanan() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"Kode", "Nama", "Bus", "Rute", "Tanggal Pesan", "Kursi", "Total", "Status"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        String sql = "SELECT p.kode_tiket, u.nama AS nama_user, b.nama_bus, j.asal, j.tujuan, "
                   + "p.created_at, "
                   + "GROUP_CONCAT(k.nomor_kursi ORDER BY k.nomor_kursi SEPARATOR ', ') AS kursi, "
                   + "p.total_bayar, p.status "
                   + "FROM pemesanan p "
                   + "JOIN users u ON p.user_id = u.id "
                   + "JOIN jadwal j ON p.jadwal_id = j.id "
                   + "JOIN bus b ON j.bus_id = b.id "
                   + "LEFT JOIN detail_pemesanan dp ON p.id = dp.pemesanan_id "
                   + "LEFT JOIN kursi k ON dp.kursi_id = k.id "
                   + "GROUP BY p.id, p.kode_tiket, u.nama, b.nama_bus, j.asal, j.tujuan, "
                   + "p.created_at, p.total_bayar, p.status "
                   + "ORDER BY p.created_at DESC";

        double totalPendapatan = 0;
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));

        try {
            Connection conn = AgenTravel.getKoneksi();
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Koneksi database tidak tersedia!");
                jTable1.setModel(model);
                setTotalPendapatan(0);
                return;
            }

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String status = rs.getString("status");
                    if (status == null || status.trim().isEmpty()) {
                        status = "Belum Terverifikasi";
                    }

                    double totalBayar = rs.getDouble("total_bayar");
                    if ("Terverifikasi".equalsIgnoreCase(status)) {
                        totalPendapatan += totalBayar;
                    }

                    String rute = rs.getString("asal") + " - " + rs.getString("tujuan");
                    String kursi = rs.getString("kursi");
                    if (kursi == null || kursi.trim().isEmpty()) {
                        kursi = "-";
                    }

                    model.addRow(new Object[]{
                        rs.getString("kode_tiket"),
                        rs.getString("nama_user"),
                        rs.getString("nama_bus"),
                        rute,
                        rs.getTimestamp("created_at"),
                        kursi,
                        "Rp " + nf.format(totalBayar),
                        status
                    });
                }
            }

            jTable1.setModel(model);
            setTotalPendapatan(totalPendapatan);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat laporan pemesanan: " + ex.getMessage());
            jTable1.setModel(model);
            setTotalPendapatan(0);
        }
    }

    private void setTotalPendapatan(double totalPendapatan) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        jTextField1.setText("TOTAL PENDAPATAN : Rp " + nf.format(totalPendapatan));
    }

    private void cetakLaporan() {
        if (jTable1.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data laporan untuk dicetak!");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Simpan Laporan Pemesanan");
        chooser.setSelectedFile(new File("Laporan-Pemesanan-Tiket.txt"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("LAPORAN PEMESANAN TIKET");
            writer.println("Tanggal Cetak: " + new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date()));
            writer.println("============================================================");

            for (int row = 0; row < jTable1.getRowCount(); row++) {
                writer.println("Kode Tiket : " + getValue(row, 0));
                writer.println("Nama       : " + getValue(row, 1));
                writer.println("Bus        : " + getValue(row, 2));
                writer.println("Rute       : " + getValue(row, 3));
                writer.println("Tgl Pesan  : " + getValue(row, 4));
                writer.println("Kursi      : " + getValue(row, 5));
                writer.println("Total      : " + getValue(row, 6));
                writer.println("Status     : " + getValue(row, 7));
                writer.println("------------------------------------------------------------");
            }

            writer.println(jTextField1.getText());
            JOptionPane.showMessageDialog(this, "Laporan berhasil disimpan: " + file.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Gagal mencetak laporan: " + ex.getMessage());
        }
    }

    private String getValue(int row, int column) {
        Object value = jTable1.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        jPanel2.setBackground(new java.awt.Color(136, 189, 164));
        jPanel2.setPreferredSize(new java.awt.Dimension(60, 84));

        jLabel1.setFont(new java.awt.Font("Perpetua Titling MT", 1, 18)); // NOI18N
        jLabel1.setText("LAPORAN PEMESANAN TIKET");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(296, 296, 296)
                .addComponent(jLabel1)
                .addContainerGap(314, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(20, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        jPanel4.setBackground(new java.awt.Color(177, 211, 185));
        jPanel4.setPreferredSize(new java.awt.Dimension(181, 88));

        jLabel2.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel2.setText("FILTER TANGGAL PESAN");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Kode", "Nama", "Bus", "Kursi", "Total", "Status"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jTextField1.setText("TOTAL PENDAPATAN : Rp. 1.750.000");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(255, 0, 0));
        jButton1.setText("CETAK LAPORAN");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(35, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 900, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        cetakLaporan();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
