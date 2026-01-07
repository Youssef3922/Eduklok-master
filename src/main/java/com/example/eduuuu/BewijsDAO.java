package com.example.eduuuu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;

public class BewijsDAO {

    /**
     * Update een agenda-item met bewijs en koppel het aan een docent.
     */
    public void saveBewijs(int agendaId, int docentId, String bewijsPath) throws Exception {
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE Agenda SET Docent_ID = ?, BewijsPath = ?, Status = ? WHERE Agenda_ID = ?")) {

            ps.setInt(1, docentId);
            ps.setString(2, bewijsPath);
            ps.setString(3, "Verstuurd");
            ps.setInt(4, agendaId);
            ps.executeUpdate();
        }
    }

    /**
     * Markeer een bewijs als nagekeken.
     */
    public void markBewijsNagekeken(int agendaId) throws SQLException {
        String sql = "UPDATE Agenda SET Status = 'Nagekeken' WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, agendaId);
            ps.executeUpdate();
        }
    }

    /**
     * Feedback opslaan voor een agenda-item.
     */
    public void saveFeedback(int agendaId, String feedback) throws SQLException {
        String sql = "UPDATE Agenda SET Feedback = ? WHERE Agenda_ID = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, feedback);
            ps.setInt(2, agendaId);
            ps.executeUpdate();
        }
    }

    /**
     * Haal alle bewijs-uploads voor een docent op, inclusief studentnaam en alle details van de opdracht.
     */
    public List<AgendaItem> getBewijsVoorDocent(int docentId) throws Exception {
        List<AgendaItem> uploads = new ArrayList<>();

        String sql = "SELECT a.Agenda_ID, a.Dag, a.Tijd, a.Activiteit, a.Werkduur, " +
                "a.BewijsPath, a.Status, a.Feedback, s.Username AS studentName " +
                "FROM Agenda a " +
                "JOIN Student s ON a.Student_ID = s.Student_ID " +
                "WHERE a.Docent_ID = ? AND a.Status = 'Verstuurd' " +
                "ORDER BY a.Dag, a.Tijd";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, docentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AgendaItem item = new AgendaItem();
                    item.setId(rs.getInt("Agenda_ID"));
                    item.setDag(rs.getString("Dag"));
                    item.setTijd(rs.getString("Tijd"));
                    item.setActiviteit(rs.getString("Activiteit"));
                    item.setWerkduur(rs.getString("Werkduur"));
                    item.setBewijsPath(rs.getString("BewijsPath"));
                    item.setStatus(rs.getString("Status"));
                    item.setStudentName(rs.getString("studentName"));
                    item.setFeedback(rs.getString("Feedback"));   // ✅ feedback ophalen
                    uploads.add(item);
                }
            }
        }
        return uploads;
    }
}