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
 * @author ASUS TUF
 */
public class jPanelKelolaBus extends javax.swing.JPanel {

    private int selectedBusId = -1; // menyimpan ID bus yang sedang dipilih

    /**
     * Creates new form jPanelKelolaBus
     */
    public jPanelKelolaBus() {
        initComponents();
        loadDataBus();
        setupTableListener();
        jButton3.addActionListener(evt -> editBus());
    }

    // ==================== LOAD DATA BUS KE TABEL ====================
    private void loadDataBus() {
        DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "NAMA BUS", "JUMLAH KURSI", "HARGA TIKET", "FASILITAS"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // tabel tidak bisa diedit langsung
            }
        };

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "SELECT * FROM bus ORDER BY id ASC";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("nama_bus"),
                    rs.getInt("jumlah_kursi"),
                    rs.getDouble("harga_tiket"),
                    rs.getString("fasilitas")
                });
            }

            jTable1.setModel(model);
            sembunyikanKolomId();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memuat data bus: " + e.getMessage());
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

        selectedBusId = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
        jTextField1.setText(getTableValue(modelRow, 1));
        jTextField4.setText(getTableValue(modelRow, 2));
        jTextField2.setText(getTableValue(modelRow, 3));
        jTextField3.setText(getTableValue(modelRow, 4));
    }

    private String getTableValue(int row, int column) {
        Object value = jTable1.getModel().getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private void sembunyikanKolomId() {
        if (jTable1.getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setMinWidth(0);
            jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
            jTable1.getColumnModel().getColumn(0).setWidth(0);
        }
    }


    // ==================== VALIDASI FORM ====================
    private boolean validasiForm() {
        String namaBus = jTextField1.getText().trim();
        String hargaTiketStr = jTextField2.getText().trim();
        String jumlahKursiStr = jTextField4.getText().trim();

        if (namaBus.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama bus wajib diisi!");
            jTextField1.requestFocus();
            return false;
        }

        if (jumlahKursiStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jumlah kursi harus diisi!");
            jTextField4.requestFocus();
            return false;
        }

        try {
            int jumlahKursi = Integer.parseInt(jumlahKursiStr);
            if (jumlahKursi <= 0) {
                JOptionPane.showMessageDialog(this, "Jumlah kursi harus lebih dari 0!");
                jTextField4.requestFocus();
                return false;
            }
            if (jumlahKursi > 26) {
                JOptionPane.showMessageDialog(this,
                    "Jumlah kursi maksimal 26!\n(Kapasitas tampilan kursi saat ini: 26 kursi)");
                jTextField4.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah kursi harus berupa angka!");
            jTextField4.requestFocus();
            return false;
        }

        if (hargaTiketStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Harga tiket wajib diisi!");
            jTextField2.requestFocus();
            return false;
        }

        try {
            double hargaTiket = Double.parseDouble(hargaTiketStr);
            if (hargaTiket <= 0) {
                JOptionPane.showMessageDialog(this, "Harga tiket harus lebih dari 0!");
                jTextField2.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Harga tiket harus berupa angka!");
            jTextField2.requestFocus();
            return false;
        }

        return true;
    }

    // ==================== BERSIHKAN FORM ====================
    private void clearForm() {
        jTextField1.setText("");
        jTextField2.setText("");
        jTextField3.setText("");
        jTextField4.setText("");
        selectedBusId = -1;
        jTable1.clearSelection();
        jTextField1.requestFocus();
    }

    // ==================== SIMPAN BUS BARU ====================
    private void simpanBus() {
        if (!validasiForm()) return;

        String namaBus = jTextField1.getText().trim();
        int jumlahKursi = Integer.parseInt(jTextField4.getText().trim());
        double hargaTiket = Double.parseDouble(jTextField2.getText().trim());
        String fasilitas = jTextField3.getText().trim();

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "INSERT INTO bus (nama_bus, jumlah_kursi, harga_tiket, fasilitas) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, namaBus);
            pst.setInt(2, jumlahKursi);
            pst.setDouble(3, hargaTiket);
            pst.setString(4, fasilitas.isEmpty() ? null : fasilitas);

            int result = pst.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Data bus berhasil disimpan!");
                clearForm();
                loadDataBus();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data bus: " + e.getMessage());
        }
    }

    // ==================== EDIT BUS ====================
    private void editBus() {
        if (selectedBusId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data bus yang ingin diedit pada tabel!");
            return;
        }

        if (!validasiForm()) return;

        String namaBus = jTextField1.getText().trim();
        int jumlahKursi = Integer.parseInt(jTextField4.getText().trim());
        double hargaTiket = Double.parseDouble(jTextField2.getText().trim());
        String fasilitas = jTextField3.getText().trim();

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "UPDATE bus SET nama_bus = ?, jumlah_kursi = ?, harga_tiket = ?, fasilitas = ? WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, namaBus);
            pst.setInt(2, jumlahKursi);
            pst.setDouble(3, hargaTiket);
            pst.setString(4, fasilitas.isEmpty() ? null : fasilitas);
            pst.setInt(5, selectedBusId);

            int result = pst.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Data bus berhasil diperbarui!");
                clearForm();
                loadDataBus();
            } else {
                JOptionPane.showMessageDialog(this, "Data bus tidak ditemukan atau sudah dihapus.");
                clearForm();
                loadDataBus();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memperbarui data bus: " + e.getMessage());
        }
    }

    // ==================== HAPUS BUS ====================
    private void hapusBus() {
        if (selectedBusId == -1) {
            JOptionPane.showMessageDialog(this, "Pilih data bus yang ingin dihapus pada tabel!");
            return;
        }

        // Cek apakah bus digunakan di jadwal
        try {
            Connection conn = AgenTravel.getKoneksi();
            String cekSql = "SELECT COUNT(*) AS total FROM jadwal WHERE bus_id = ?";
            PreparedStatement pstCek = conn.prepareStatement(cekSql);
            pstCek.setInt(1, selectedBusId);
            ResultSet rsCek = pstCek.executeQuery();

            if (rsCek.next() && rsCek.getInt("total") > 0) {
                JOptionPane.showMessageDialog(this,
                    "Bus tidak dapat dihapus karena masih digunakan pada jadwal.");
                return;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal memeriksa jadwal: " + e.getMessage());
            return;
        }

        // Konfirmasi hapus
        int confirm = JOptionPane.showConfirmDialog(this,
            "Apakah Anda yakin ingin menghapus bus ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = AgenTravel.getKoneksi();
            String sql = "DELETE FROM bus WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, selectedBusId);

            int result = pst.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this, "Data bus berhasil dihapus!");
                clearForm();
                loadDataBus();
            } else {
                JOptionPane.showMessageDialog(this, "Data bus tidak ditemukan atau sudah dihapus.");
                clearForm();
                loadDataBus();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal menghapus data bus: " + e.getMessage());
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

        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel7 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "NAMA BUS", "JUMLAH KURSI", "HARGA TIKET", "FASILITAS"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jPanel7.setBackground(new java.awt.Color(101, 146, 135));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Perpetua Titling MT", 1, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Kelola Bus");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(349, 349, 349))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel1)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(177, 211, 185));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FORM DATA BUS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Perpetua Titling MT", 1, 12), new java.awt.Color(255, 255, 255))); // NOI18N
        jPanel1.setName(""); // NOI18N

        jLabel2.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("harga tiket");

        jLabel4.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("fasilitas");

        jLabel5.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("nama bus");

        jLabel6.setFont(new java.awt.Font("Perpetua Titling MT", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("jumlah kursi");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                    .addComponent(jTextField1)
                    .addComponent(jTextField4))
                .addGap(56, 56, 56)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        jButton1.setBackground(new java.awt.Color(255, 51, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("HAPUS");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(0, 204, 0));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("SIMPAN");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setBackground(new java.awt.Color(255, 255, 51));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("EDIT");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 183, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(35, 35, 35))
                    .addComponent(jPanel7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(79, 79, 79)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel1.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        simpanBus();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        hapusBus();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        editBus();
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
