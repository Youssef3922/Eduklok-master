package com.example.eduuuu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/educatie_db";
    private static final String USER = "root";       // jouw MySQL gebruikersnaam
    private static final String PASSWORD = "Voetbalpro1234"; // jouw MySQL wachtwoord

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}