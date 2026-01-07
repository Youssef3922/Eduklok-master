package com.example.eduuuu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AgendaDAO {

    // Item opslaan MET docent
    public void saveAgendaItem(int studentId, int docentId, int week,
                               String dag, String tijd, String activiteit, String werkduur) throws SQLException {
        String sql = "INSERT INTO Agenda (Student_ID, Docent_ID, Week, Dag, Tijd, Activiteit, Werkduur, Status, BewijsPath, Feedback) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, 'Nog te doen', NULL, NULL)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, docentId);
            ps.setInt(3, week);
            ps.setString(4, dag);
            ps.setString(5, tijd);
            ps.setString(6, activiteit);
            ps.setString(7, werkduur);
            ps.executeUpdate();
        }
    }

    // Item opslaan ZONDER docent
    public void saveAgendaItem(int studentId, int week,
                               String dag, String tijd, String activiteit, String werkduur) throws SQLException {
        String sql = "INSERT INTO Agenda (Student_ID, Week, Dag, Tijd, Activiteit, Werkduur, Status, BewijsPath, Feedback) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'Nog te doen', NULL, NULL)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, week);
            ps.setString(3, dag);
            ps.setString(4, tijd);
            ps.setString(5, activiteit);
            ps.setString(6, werkduur);
            ps.executeUpdate();
        }
    }

    // Bewijs + docent koppelen aan agenda-item
    public void saveBewijsVoorDocent(int agendaId, String path, int docentId) throws SQLException {
        String sql = "UPDATE Agenda SET BewijsPath = ?, Docent_ID = ? WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, path);
            ps.setInt(2, docentId);
            ps.setInt(3, agendaId);
            ps.executeUpdate();
        }
    }

    // Status bijwerken
    public void updateStatus(int agendaId, String newStatus) throws SQLException {
        String sql = "UPDATE Agenda SET Status = ? WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, agendaId);
            ps.executeUpdate();
        }
    }

    // Docent wijzigen (los van bewijs)
    public void updateDocent(int agendaId, int docentId) throws SQLException {
        String sql = "UPDATE Agenda SET Docent_ID = ? WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, docentId);
            ps.setInt(2, agendaId);
            ps.executeUpdate();
        }
    }

    // Item verwijderen
    public void deleteAgendaItem(int agendaId) throws SQLException {
        String sql = "DELETE FROM Agenda WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agendaId);
            ps.executeUpdate();
        }
    }

    // Ophalen: alle items per student/week (OBJECTEN)
    public List<AgendaItem> getAgendaItemsFull(int studentId, int week) throws SQLException {
        List<AgendaItem> items = new ArrayList<>();
        String sql = "SELECT a.Agenda_ID, a.Dag, a.Tijd, a.Activiteit, a.Werkduur, a.Status, a.BewijsPath, a.Feedback, " +
                "a.Docent_ID, d.Username AS DocentNaam " +
                "FROM Agenda a LEFT JOIN Docent d ON a.Docent_ID = d.Docent_ID " +
                "WHERE a.Student_ID = ? AND a.Week = ? ORDER BY a.Dag, a.Tijd";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, week);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    // Zet een agenda-item op 'Afgerond'
    public void markAgendaItemAfgerond(int agendaId) throws SQLException {
        String sql = "UPDATE Agenda SET Status = 'Afgerond' WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agendaId);
            ps.executeUpdate();
        }
    }

    public void markAgendaItemNagekeken(int id) throws SQLException {
        String sql = "UPDATE Agenda SET Status = 'Nagekeken' WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // (Optioneel) terugzetten naar 'Nog te doen'
    public void markAgendaItemNogTeDoen(int agendaId) throws SQLException {
        String sql = "UPDATE Agenda SET Status = 'Nog te doen' WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agendaId);
            ps.executeUpdate();
        }
    }

    // Ophalen: alle opdrachten (ongeacht student/week)
    public List<AgendaItem> getAllOpdrachten() throws SQLException {
        List<AgendaItem> items = new ArrayList<>();
        String sql = "SELECT a.Agenda_ID, a.Dag, a.Tijd, a.Activiteit, a.Werkduur, a.Status, a.BewijsPath, a.Feedback, " +
                "a.Docent_ID, d.Username AS DocentNaam " +
                "FROM Agenda a LEFT JOIN Docent d ON a.Docent_ID = d.Docent_ID " +
                "ORDER BY a.Week, a.Dag, a.Tijd";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                items.add(mapRow(rs));
            }
        }
        return items;
    }

    // Ophalen: alle items per student/week/dag (OBJECTEN)
    public List<AgendaItem> getAgendaItems(int studentId, int week, String dag) throws SQLException {
        List<AgendaItem> items = new ArrayList<>();
        String sql = "SELECT a.Agenda_ID, a.Dag, a.Tijd, a.Activiteit, a.Werkduur, a.Status, a.BewijsPath, a.Feedback, " +
                "a.Docent_ID, d.Username AS DocentNaam " +
                "FROM Agenda a LEFT JOIN Docent d ON a.Docent_ID = d.Docent_ID " +
                "WHERE a.Student_ID = ? AND a.Week = ? AND a.Dag = ? ORDER BY a.Tijd";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, week);
            ps.setString(3, dag);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
            }
        }
        return items;
    }

    // Ophalen: één item op ID
    public AgendaItem getAgendaItemById(int agendaId) throws SQLException {
        String sql = "SELECT a.Agenda_ID, a.Dag, a.Tijd, a.Activiteit, a.Werkduur, a.Status, a.BewijsPath, a.Feedback, " +
                "a.Docent_ID, d.Username AS DocentNaam " +
                "FROM Agenda a LEFT JOIN Docent d ON a.Docent_ID = d.Docent_ID " +
                "WHERE a.Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agendaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    // Helper: map ResultSet → AgendaItem
    private AgendaItem mapRow(ResultSet rs) throws SQLException {
        AgendaItem item = new AgendaItem();
        item.setId(rs.getInt("Agenda_ID"));
        item.setDag(rs.getString("Dag"));
        item.setTijd(rs.getString("Tijd"));
        item.setActiviteit(rs.getString("Activiteit"));
        item.setWerkduur(rs.getString("Werkduur"));
        item.setStatus(rs.getString("Status"));
        item.setBewijsPath(rs.getString("BewijsPath"));
        item.setFeedback(rs.getString("Feedback"));      // ✅ feedback meenemen
        item.setDocentId(rs.getInt("Docent_ID"));
        item.setDocentNaam(rs.getString("DocentNaam"));
        return item;
    }
}