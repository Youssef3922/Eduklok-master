package com.example.eduuuu;
import java.sql.*;
import java.util.*;

public class AgendaDAO {
    private Connection conn;

    public AgendaDAO() throws SQLException {
        conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/educlockapp", "root", "jouw_wachtwoord");
    }

    public void saveAgendaItem(int studentId, int week, String dag, String tijd, String activiteit) throws SQLException {
        String sql = "INSERT INTO agenda (student_id, week, dag, tijd, activiteit) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, week);
            stmt.setString(3, dag);
            stmt.setString(4, tijd);
            stmt.setString(5, activiteit);
            stmt.executeUpdate();
        }
    }

    public List<String> getAgendaItems(int studentId, int week, String dag) throws SQLException {
        List<String> items = new ArrayList<>();
        String sql = "SELECT tijd, activiteit FROM agenda WHERE student_id=? AND week=? AND dag=? ORDER BY tijd";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            stmt.setInt(2, week);
            stmt.setString(3, dag);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(rs.getString("tijd") + " - " + rs.getString("activiteit"));
                }
            }
        }
        return items;
    }
}