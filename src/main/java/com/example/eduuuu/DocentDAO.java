package com.example.eduuuu;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.mindrot.jbcrypt.BCrypt;

public class DocentDAO {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Overload: voeg docent toe met alleen username en password.
     * Naam wordt standaard gelijk aan username.
     */
    public boolean addDocent(String username, String password) {
        return addDocent(username, password, username);
    }

    /**
     * Voeg een nieuwe docent toe aan de database.
     * Wachtwoord wordt gehashed met BCrypt voordat het wordt opgeslagen.
     * Controleert eerst of de gebruikersnaam al bestaat.
     */
    public boolean addDocent(String username, String password, String naam) {
        String checkSql = "SELECT COUNT(*) FROM Docent WHERE Username = ?";
        String insertSql = "INSERT INTO Docent (Username, Password, Naam, FailedAttempts, AccountLocked, LockTime) VALUES (?, ?, ?, 0, FALSE, NULL)";
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Gebruikersnaam bestaat al
                    return false;
                }
            }

            // Nieuwe docent toevoegen
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                insertPs.setString(1, username);
                insertPs.setString(2, hashedPassword);
                insertPs.setString(3, naam);

                return insertPs.executeUpdate() > 0;
            }

        } catch (Exception e) {
            System.out.println("❌ Docent opslaan mislukt: " + e.getMessage());
            return false;
        }
    }

    /**
     * Controleer login en geef een Docent object terug bij succes.
     * - Bij fout wachtwoord: aftellen hoeveel pogingen nog over zijn.
     * - Bij 5 fouten: account 30 minuten blokkeren.
     * - Tijdens blokkering: melding met resterende minuten.
     */
    public Docent checkLogin(String username, String password) throws Exception {
        String sql = "SELECT Docent_ID, Username, Naam, Password, FailedAttempts, AccountLocked, LockTime FROM Docent WHERE Username = ?";
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
                                throw new Exception("❌ Account is tijdelijk geblokkeerd. Wacht nog " + remaining + " minuten.");
                            }
                        }
                    }

                    // Check wachtwoord
                    if (BCrypt.checkpw(password, storedHash)) {
                        resetFailedAttempts(username, conn);
                        // ✅ Login geslaagd → maak Docent object
                        int id = rs.getInt("Docent_ID");
                        String naam = rs.getString("Naam");
                        return new Docent(id, username, naam);
                    } else {
                        incrementFailedAttempts(username, attempts, conn);
                        int remainingAttempts = MAX_FAILED_ATTEMPTS - (attempts + 1);
                        if (remainingAttempts > 0) {
                            throw new Exception("❌ Fout wachtwoord. Je hebt nog " + remainingAttempts + " pogingen.");
                        } else {
                            throw new Exception("❌ Alle pogingen op. Account is 30 minuten geblokkeerd.");
                        }
                    }
                }
            }
        }
        throw new Exception("❌ Gebruiker niet gevonden.");
    }

    private void resetFailedAttempts(String username, Connection conn) throws Exception {
        String sql = "UPDATE Docent SET FailedAttempts = 0, AccountLocked = FALSE, LockTime = NULL WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.executeUpdate();
        }
    }

    private void incrementFailedAttempts(String username, int currentAttempts, Connection conn) throws Exception {
        int newAttempts = currentAttempts + 1;
        boolean lock = newAttempts >= MAX_FAILED_ATTEMPTS;

        String sql = "UPDATE Docent SET FailedAttempts = ?, AccountLocked = ?, LockTime = ? WHERE Username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newAttempts);
            ps.setBoolean(2, lock);
            ps.setTimestamp(3, lock ? new Timestamp(System.currentTimeMillis()) : null);
            ps.setString(4, username);
            ps.executeUpdate();
        }
    }

    /**
     * Haal alle docenten op uit de database (voor ComboBox of beheer).
     */
    public List<Docent> getAllDocenten() {
        String sql = "SELECT Docent_ID, Username, Naam FROM Docent";
        List<Docent> docenten = new ArrayList<>();

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("Docent_ID");
                String username = rs.getString("Username");
                String naam = rs.getString("Naam");

                docenten.add(new Docent(id, username, naam));
            }
        } catch (Exception e) {
            System.out.println("❌ Fout bij ophalen docenten: " + e.getMessage());
        }
        return docenten;
    }
}