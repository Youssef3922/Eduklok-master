package com.example.eduuuu;

public class Docent {
    private int id;
    private String username;
    private String naam;

    public Docent(int id, String username, String naam) {
        this.id = id;
        this.username = username;
        this.naam = naam;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getNaam() { return naam; }

    @Override
    public String toString() {
        return naam; // zodat ComboBox de naam toont
    }
}