module Eduklok.master {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;// voor je database

    opens com.example.eduuuu to javafx.fxml;
    exports com.example.eduuuu;
}