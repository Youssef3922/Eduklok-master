package com.example.eduuuu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class gebruikerDAO {

    // Nieuwe gebruiker opslaan
    public boolean addGebruiker(String username, String password) {
        String sql = "INSERT INTO gebruiker (Username, Password) VALUES (?, ?)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.out.println("❌ Opslaan mislukt: " + e.getMessage());
            return false;
        }
    }

    // Check of login klopt
    public boolean checkLogin(String username, String password) {
        String sql = "SELECT 1 FROM Gebruiker WHERE Username=? AND Password=?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true als er een match is
            }
        } catch (Exception e) {
            System.out.println("❌ Inloggen mislukt: " + e.getMessage());
            return false;
        }
    }
}