package com.example.eduuuu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.mindrot.jbcrypt.BCrypt;

public class StudentDAO {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Voeg een nieuwe student toe aan de database.
     * Wachtwoord wordt gehashed met BCrypt voordat het wordt opgeslagen.
     * Controleert eerst of de gebruikersnaam al bestaat.
     */
    public boolean addStudent(String username, String password) {
        String checkSql = "SELECT COUNT(*) FROM Student WHERE Username = ?";
        String insertSql = "INSERT INTO Student (Username, Password, FailedAttempts, AccountLocked, LockTime) VALUES (?, ?, 0, FALSE, NULL)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Gebruikersnaam bestaat al
                    return false;
                }
            }

            // Nieuwe student toevoegen
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, username);
                insertPs.setString(2, hashedPassword);
                return insertPs.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.out.println("❌ Student opslaan mislukt: " + e.getMessage());
            return false;
        }
    }

    /**
     * Controleer login en geef een bericht terug.
     * - Bij fout wachtwoord: aftellen hoeveel pogingen nog over zijn.
     * - Bij 5 fouten: account 30 minuten blokkeren.
     * - Tijdens blokkering: melding met resterende minuten.
     */
    public String checkLogin(String username, String password) {
        String sql = "SELECT Password, FailedAttempts, AccountLocked, LockTime FROM Student WHERE Username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean locked = rs.getBoolean("AccountLocked");
                    int attempts = rs.getInt("FailedAttempts");
                    Timestamp lockTime = rs.getTimestamp("LockTime");
                    String storedHash = rs.getString("Password");

                    // Check lockout
                    if (locked) {
                        if (lockTime != null) {
                            long diffMinutes = (System.currentTimeMillis() - lockTime.getTime()) / (1000 * 60);
                            if (diffMinutes >= LOCKOUT_DURATION_MINUTES) {
                                resetFailedAttempts(username, conn);
                            } else {
                                long remaining = LOCKOUT_DURATION_MINUTES - diffMinutes;
                                return "❌ Account is tijdelijk geblokkeerd. Wacht nog " + remaining + " minuten.";
                            }
                        }
                    }

                    // Check wachtwoord
                    if (BCrypt.checkpw(password, storedHash)) {
                        resetFailedAttempts(username, conn);
                        return "✅ Login geslaagd!";
                    } else {
                        incrementFailedAttempts(username, attempts, conn);
                        int remainingAttempts = MAX_FAILED_ATTEMPTS - (attempts + 1);
                        if (remainingAttempts > 0) {
                            return "❌ Fout wachtwoord. Je hebt nog " + remainingAttempts + " pogingen.";
                        } else {
                            return "❌ Alle pogingen op. Account is 30 minuten geblokkeerd.";
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "❌ Student inloggen mislukt: " + e.getMessage();
        }
        return "❌ Gebruiker niet gevonden.";
    }

    /**
     * Haal het Student_ID op voor een gegeven gebruikersnaam.
     * Dit gebruik je om currentUserId te zetten bij login.
     */
    public int getStudentId(String username) {
        String sql = "SELECT Student_ID FROM Student WHERE Username = ?";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Student_ID");
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Ophalen Student_ID mislukt: " + e.getMessage());
        }
        return -1;
    }

    private void resetFailedAttempts(String username, Connection conn) throws Exception {
        String sql = "UPDATE Student SET FailedAttempts = 0, AccountLocked = FALSE, LockTime = NULL WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    private void incrementFailedAttempts(String username, int currentAttempts, Connection conn) throws Exception {
        int newAttempts = currentAttempts + 1;
        boolean lock = newAttempts >= MAX_FAILED_ATTEMPTS;

        String sql = "UPDATE Student SET FailedAttempts = ?, AccountLocked = ?, LockTime = ? WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newAttempts);
            ps.setBoolean(2, lock);
            ps.setTimestamp(3, lock ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setString(4, username);
            ps.executeUpdate();
        }
    }
}