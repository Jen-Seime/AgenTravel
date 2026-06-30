/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package agentravel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/**
 *
 * @author ASUS
 */
public class AgenTravel {
    
    private static Connection koneksi;

    public static Connection getKoneksi() {
        
        
        try {
            if (koneksi == null || koneksi.isClosed()) {

                String url = "jdbc:mysql://localhost:3306/travel_bus_db";
                String user = "root";
                String password = "";

                Class.forName("com.mysql.cj.jdbc.Driver");
                koneksi = DriverManager.getConnection(url, user, password);
            }
        } catch (ClassNotFoundException | SQLException e) {
            JOptionPane.showMessageDialog(null, "Koneksi Database Gagal: " + e.getMessage());
        }

        return koneksi;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}