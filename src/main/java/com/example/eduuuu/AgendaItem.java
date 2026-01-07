package com.example.eduuuu;

public class AgendaItem {
    private int id;
    private String dag;
    private String tijd;
    private String activiteit;
    private String werkduur;
    private String status;
    private String bewijsPath;
    private int docentId;
    private String docentNaam;
    private String titel;
    private String studentName;   // ✅ nieuw veld voor leerlingnaam
    private String feedback;      // ✅ optioneel veld voor feedback van docent

    // ✅ Helpers om status te checken
    public boolean isAfgerond() {
        return status != null && status.equalsIgnoreCase("Afgerond");
    }

    public boolean isNogTeDoen() {
        return status != null && status.equalsIgnoreCase("Nog te doen");
    }

    // ✅ Setter om lokaal status bij te werken
    public void setAfgerond(boolean afgerond) {
        this.status = afgerond ? "Afgerond" : "Nog te doen";
    }

    // Getters en setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDag() { return dag; }
    public void setDag(String dag) { this.dag = dag; }

    public String getTijd() { return tijd; }
    public void setTijd(String tijd) { this.tijd = tijd; }

    public String getActiviteit() { return activiteit; }
    public void setActiviteit(String activiteit) { this.activiteit = activiteit; }

    public String getWerkduur() { return werkduur; }
    public void setWerkduur(String werkduur) { this.werkduur = werkduur; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBewijsPath() { return bewijsPath; }
    public void setBewijsPath(String bewijsPath) { this.bewijsPath = bewijsPath; }

    public int getDocentId() { return docentId; }
    public void setDocentId(int docentId) { this.docentId = docentId; }

    public String getDocentNaam() { return docentNaam; }
    public void setDocentNaam(String docentNaam) { this.docentNaam = docentNaam; }

    public String getTitel() { return titel; }
    public void setTitel(String titel) { this.titel = titel; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    // ToString voor weergave in ListView of Label
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(dag).append(" ").append(tijd)
                .append(" — ").append(activiteit)
                .append(" (").append(werkduur).append(")")
                .append(" [").append(status).append("]");

        if (studentName != null && !studentName.isBlank()) {
            sb.append(" (Student: ").append(studentName).append(")");
        }
        if (docentNaam != null && !docentNaam.isBlank()) {
            sb.append(" (Docent: ").append(docentNaam).append(")");
        }
        return sb.toString();
    }
}