package com.example.eduuuu;

import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.animation.*;
import javafx.util.Duration;
import java.io.File;
import java.time.LocalTime;
import java.util.Optional;
import java.util.List;
import java.sql.SQLException;
import java.util.Arrays;
import javafx.scene.Node;
import java.awt.Desktop;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import java.io.File;



public class EduClockApp extends Application {
    private Stage primaryStage;
    private BorderPane mainLayout;
    private String currentUser = "Gast";
    // Globale variabelen
    private int currentUserId = -1; // ID van ingelogde student, nodig voor AgendaDAO
    private VBox sidebar;
    private boolean isDarkMode = false;
    private Timeline clockTimeline;

    // Lesson schedule (editable)
    private java.util.List<String[]> lessonSchedule = new java.util.ArrayList<>(java.util.Arrays.asList(
            new String[]{"08:30", "09:30", "Wiskunde"},
            new String[]{"09:45", "10:45", "Nederlands"},
            new String[]{"11:00", "12:00", "Engels"},
            new String[]{"13:00", "14:00", "Geschiedenis"},
            new String[]{"14:30", "15:30", "Wiskunde"},
            new String[]{"15:45", "16:45", "Biologie"}
    ));

    // Color schemes
    private String lightBg = "#f5f6f7";
    private String darkBg = "#1a1a2e";
    private String lightSidebar = "#2d2d3c";
    private String darkSidebar = "#0f0f1e";
    private String lightCard = "white";
    private String darkCard = "#16213e";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("EduClock - Platform");
        showLoginScreen();


    }


    // Helper voor mooie alerts
    private void showStyledAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #ffecd2, #fcb69f);" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-text-fill: #333;"
        );

        dialogPane.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #ff6f61;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 6 12;"
        );

        alert.showAndWait();
    }






    private String currentRole = "GAST"; // wordt automatisch aangepast bij login

    // ==================== LOGIN SCREEN ====================
    // ==================== LOGIN SCREEN ====================
    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea, #764ba2);");
        root.setPadding(new Insets(40));

        VBox loginCard = new VBox(25);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(420);
        loginCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 50 40;");
        loginCard.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        Label title = new Label("EduClock");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        title.setTextFill(Color.web("#667eea"));

        Label subtitle = new Label("Welkom terug!");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.GRAY);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Gebruikersnaam");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Wachtwoord");

        Button loginBtn = new Button("Inloggen");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8;");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showStyledAlert(Alert.AlertType.WARNING, "Inloggen mislukt", "Voer gebruikersnaam én wachtwoord in!");
                return;
            }

            StudentDAO dao = new StudentDAO();
            String resultMessage = dao.checkLogin(username, password); // DAO geeft bericht terug

            if (resultMessage.contains("✅")) {
                // ✅ Alleen deze drie regels zijn toegevoegd
                currentUser = username;
                currentRole = "STUDENT";                 // rol instellen voor sidebar
                currentUserId = dao.getStudentId(username); // ID ophalen voor AgendaDAO

                showMainApp();
            } else {
                showStyledAlert(Alert.AlertType.ERROR, "Inloggen mislukt", resultMessage);
            }
        });

        Hyperlink registerLink = new Hyperlink("Nog geen account? Registreer hier");
        registerLink.setFont(Font.font("Arial", 12));
        registerLink.setTextFill(Color.web("#667eea"));
        registerLink.setOnAction(ev -> showRegisterScreen());

        Hyperlink teacherLoginLink = new Hyperlink("Login als docent");
        teacherLoginLink.setFont(Font.font("Arial", 12));
        teacherLoginLink.setTextFill(Color.web("#667eea"));
        teacherLoginLink.setOnAction(ev -> showTeacherLoginScreen());

        loginCard.getChildren().addAll(title, subtitle, usernameField, passwordField, loginBtn, registerLink, teacherLoginLink);
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setScene(scene);
        root.requestFocus();
        primaryStage.show();
    }

    // ==================== REGISTER SCREEN ====================
    private void showRegisterScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #43cea2, #185a9d);");
        root.setPadding(new Insets(40));

        VBox registerCard = new VBox(25);
        registerCard.setAlignment(Pos.CENTER);
        registerCard.setMaxWidth(420);
        registerCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 50 40;");
        registerCard.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        Label title = new Label("Registreren");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#185a9d"));

        Label subtitle = new Label("Maak een nieuw account aan");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setTextFill(Color.GRAY);

        TextField usernameField = new TextField();
        usernameField.setPromptText("Gebruikersnaam");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Wachtwoord");

        Button registerBtn = new Button("Account aanmaken");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setStyle("-fx-background-color: #185a9d; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8;");

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showStyledAlert(Alert.AlertType.WARNING, "Registreren mislukt", "Beide velden zijn verplicht!");
                return;
            }

            StudentDAO dao = new StudentDAO();
            if (dao.addStudent(username, password)) {
                showStyledAlert(Alert.AlertType.INFORMATION, "Succes", "✅ Account aangemaakt! Je kunt nu inloggen.");
                showLoginScreen();
            } else {
                showStyledAlert(Alert.AlertType.WARNING, "Registreren mislukt", "❌ Deze gebruikersnaam bestaat al. Kies een andere!");
            }
        });

        Hyperlink backToLogin = new Hyperlink("Terug naar inloggen");
        backToLogin.setOnAction(ev -> showLoginScreen());

        registerCard.getChildren().addAll(title, subtitle, usernameField, passwordField, registerBtn, backToLogin);
        root.getChildren().add(registerCard);

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setScene(scene);
        root.requestFocus();
        primaryStage.show();
    }



    // ==================== DOCENT LOGIN SCREEN ====================
    // Voeg bovenin je EduClockApp toe:
    private Docent currentDocent; // ingelogde docent

    private void showTeacherLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2193b0, #6dd5ed);");
        root.setPadding(new Insets(40));

        VBox loginCard = new VBox(25);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(420);
        loginCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 50 40;");
        loginCard.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        Label title = new Label("Docent Login");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setTextFill(Color.web("#2193b0"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Docent gebruikersnaam");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Wachtwoord");

        Button loginBtn = new Button("Inloggen als docent");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: #2193b0; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8;");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showStyledAlert(Alert.AlertType.WARNING, "Inloggen mislukt", "Voer gebruikersnaam én wachtwoord in!");
                return;
            }

            try {
                DocentDAO dao = new DocentDAO();
                Docent docent = dao.checkLogin(username, password); // ✅ geeft een Docent object terug

                if (docent != null) {
                    currentDocent = docent;          // ✅ ingelogde docent opslaan
                    currentUser = docent.getUsername();
                    currentRole = "DOCENT";          // rol instellen
                    showMainApp();                   // ga naar dashboard
                } else {
                    showStyledAlert(Alert.AlertType.ERROR, "Inloggen mislukt", "Onjuiste gebruikersnaam of wachtwoord");
                }
            } catch (Exception ex) {
                showStyledAlert(Alert.AlertType.ERROR, "Login fout", ex.getMessage());
            }
        });

        Hyperlink backToStudentLogin = new Hyperlink("Terug naar student login");
        backToStudentLogin.setFont(Font.font("Arial", 12));
        backToStudentLogin.setTextFill(Color.web("#2193b0"));
        backToStudentLogin.setOnAction(ev -> showLoginScreen());

        loginCard.getChildren().addAll(title, usernameField, passwordField, loginBtn, backToStudentLogin);
        root.getChildren().add(loginCard);

        Scene scene = new Scene(root, 1200, 750);
        primaryStage.setScene(scene);
        root.requestFocus();
        primaryStage.show();
    }

        // ==================== DOCENT REGISTER SCREEN ====================
        private void showTeacherRegisterScreen() {
            VBox root = new VBox(20);
            root.setAlignment(Pos.CENTER);
            root.setStyle("-fx-background-color: linear-gradient(to bottom right, #6dd5ed, #2193b0);");
            root.setPadding(new Insets(40));

            VBox registerCard = new VBox(25);
            registerCard.setAlignment(Pos.CENTER);
            registerCard.setMaxWidth(420);
            registerCard.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 50 40;");
            registerCard.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

            Label title = new Label("Docent Registratie");
            title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            title.setTextFill(Color.web("#2193b0"));

            Label subtitle = new Label("Maak een nieuw docent account aan");
            subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            subtitle.setTextFill(Color.GRAY);

            TextField usernameField = new TextField();
            usernameField.setPromptText("Docent gebruikersnaam");

            PasswordField passwordField = new PasswordField();
            passwordField.setPromptText("Wachtwoord");

            Button registerBtn = new Button("Docent account aanmaken");
            registerBtn.setMaxWidth(Double.MAX_VALUE);
            registerBtn.setStyle("-fx-background-color: #2193b0; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 8;");

            registerBtn.setOnAction(e -> {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    showStyledAlert(Alert.AlertType.WARNING, "Registreren mislukt", "Beide velden zijn verplicht!");
                    return;
                }

                DocentDAO dao = new DocentDAO();
                if (dao.addDocent(username, password)) {
                    showStyledAlert(Alert.AlertType.INFORMATION, "Succes", "✅ Docent account aangemaakt! Je kunt nu inloggen.");
                    showTeacherLoginScreen();
                } else {
                    showStyledAlert(Alert.AlertType.WARNING, "Registreren mislukt", "❌ Deze gebruikersnaam bestaat al. Kies een andere!");
                }
            });

            Hyperlink backToLogin = new Hyperlink("Terug naar docent login");
            backToLogin.setOnAction(ev -> showTeacherLoginScreen());

            registerCard.getChildren().addAll(title, subtitle, usernameField, passwordField, registerBtn, backToLogin);
            root.getChildren().add(registerCard);

            Scene scene = new Scene(root, 1200, 750);
            primaryStage.setScene(scene);
            root.requestFocus();
            primaryStage.show();
        }



    // ==================== MAIN APP ====================
    // ==================== MAIN APP ====================
    // ==================== MAIN APP ====================
    private void showMainApp() {
        mainLayout = new BorderPane();
        sidebar = createSidebar();
        mainLayout.setLeft(sidebar);
        showHomeView();

        Scene scene = new Scene(mainLayout, 1200, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    // ==================== SIDEBAR ====================
    // ==================== SIDEBAR ====================
    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(30));
        sidebar.setStyle("-fx-background-color: #2c2f4a;");

        Label userLabel = new Label("Gebruiker: " + currentUser);
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        sidebar.getChildren().add(userLabel);
        sidebar.getChildren().add(new Separator());

        // Tabs per rol
        if ("STUDENT".equalsIgnoreCase(currentRole)) {
            sidebar.getChildren().addAll(
                    createMenuButton("🏠 Home", this::showHomeView),
                    createMenuButton("📅 Agenda", this::showAgendaView),
                    createMenuButton("📝 Opdrachten", this::showOpdrachtenView),
                    // ✅ Nieuwe knop toegevoen
                    createMenuButton("⚙️ Instellingen", this::showSettings),
                    createMenuButton("🚪 Uitloggen", () -> {
                        currentUser = "Gast";
                        currentRole = "GAST";
                        showLoginScreen();
                    })
            );
        } else if ("DOCENT".equalsIgnoreCase(currentRole)) {
            sidebar.getChildren().addAll(
                    createMenuButton("🏠 Home", this::showHomeView),
                    createMenuButton("👥 Leerling-Data", this::showBeoordelingView),
                    createMenuButton("⚙️ Instellingen", this::showSettings),
                    createMenuButton("🚪 Uitloggen", () -> {
                        currentUser = "Gast";
                        currentRole = "GAST";
                        showLoginScreen();
                    })
            );
        }

        return sidebar;
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: #46465a; -fx-text-fill: white; -fx-font-size: 14; "
                + "-fx-padding: 12 20; -fx-background-radius: 5;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #5555aa; -fx-text-fill: white; -fx-font-size: 14; "
                        + "-fx-padding: 12 20; -fx-background-radius: 5;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #46465a; -fx-text-fill: white; -fx-font-size: 14; "
                        + "-fx-padding: 12 20; -fx-background-radius: 5;"
        ));

        btn.setOnAction(e -> action.run());
        return btn;
    }

    // ==================== BEWIJS UPLOAD VIEW ====================




    // ==================== ENHANCED HOME VIEW ====================
    private void showHomeView() {
        BorderPane home = new BorderPane();
        home.setPadding(new Insets(40));

        // Top section with welcome
        VBox topSection = new VBox(20);
        topSection.setAlignment(Pos.CENTER);

        Label welcomeTitle = new Label("Welkom bij EduClock");
        welcomeTitle.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        welcomeTitle.setTextFill(Color.BLACK); // zwart

        Label welcomeUser = new Label("Ingelogd als: " + currentUser + " 👋");
        welcomeUser.setFont(Font.font("Arial", FontWeight.NORMAL, 20));
        welcomeUser.setTextFill(Color.DARKGRAY);

        topSection.getChildren().addAll(welcomeTitle, welcomeUser);

        // ✅ Connectie-kader alleen voor studenten
        if ("STUDENT".equalsIgnoreCase(currentRole)) {
            VBox connectBox = new VBox(15);
            connectBox.setAlignment(Pos.CENTER);
            connectBox.setPadding(new Insets(20));
            connectBox.setStyle("-fx-background-color: " + (isDarkMode ? darkCard : lightCard) +
                    "; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);");

            Label connectLabel = new Label("EduClock Connectie");
            connectLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            connectLabel.setTextFill(isDarkMode ? Color.WHITE : Color.web("#2d2d3c"));

            Button testBtn = new Button("Test je EduClock connectie");
            testBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-size: 14; "
                    + "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8;");

            // Voor nu: simpele alert, later hardware koppelen
            testBtn.setOnAction(e -> {
                showStyledAlert(Alert.AlertType.INFORMATION, "Connectie test",
                        "✅ Je EduClock connectie test is uitgevoerd!");
            });

            connectBox.getChildren().addAll(connectLabel, testBtn);
            topSection.getChildren().add(connectBox);
        }

        // Dashboard cards
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(30, 0, 0, 0));

        // 🔑 Toon kaarten afhankelijk van rol
        if ("STUDENT".equalsIgnoreCase(currentRole)) {
            grid.add(createEnhancedDashboardCard("📅", "Agenda", "Bekijk en beheer je agenda", () -> showAgendaView()), 0, 0);
            grid.add(createEnhancedDashboardCard("📝", "Opdrachten", "Upload je bewijs en opdrachten", () -> showOpdrachtenView()), 1, 0);
            grid.add(createEnhancedDashboardCard("⚙️", "Instellingen", "Pas je profiel en voorkeuren aan", () -> showSettings()), 0, 1);
            grid.add(createEnhancedDashboardCard("🚪", "Uitloggen", "Log uit en keer terug naar login", () -> {
                currentUser = "Gast";
                currentRole = "GAST";
                showLoginScreen();
            }), 1, 1);
        } else if ("DOCENT".equalsIgnoreCase(currentRole)) {
            grid.add(createEnhancedDashboardCard("👥", "Leerlingen", "Bekijk en geef beoordelingen", () -> showBeoordelingView()), 0, 0);
            grid.add(createEnhancedDashboardCard("⚙️", "Instellingen", "Pas je profiel en voorkeuren aan", () -> showSettings()), 1, 0);
            grid.add(createEnhancedDashboardCard("🚪", "Uitloggen", "Log uit en keer terug naar login", () -> {
                currentUser = "Gast";
                currentRole = "GAST";
                showLoginScreen();
            }), 0, 1);
        } else {
            // Gast of onbekende rol → toon alles
            grid.add(createEnhancedDashboardCard("📅", "Agenda", "Bekijk en beheer je rooster", () -> showAgendaView()), 0, 0);
            grid.add(createEnhancedDashboardCard("📝", "Opdrachten", "Upload je bewijs en opdrachten", () -> showOpdrachtenView()), 1, 0);
            grid.add(createEnhancedDashboardCard("👥", "Leerlingen", "Bekijk en geef beoordelingen", () -> showBeoordelingView()), 0, 1);
            grid.add(createEnhancedDashboardCard("⚙️", "Instellingen", "Pas je profiel en voorkeuren aan", () -> showSettings()), 1, 1);
            grid.add(createEnhancedDashboardCard("🚪", "Uitloggen", "Log uit en keer terug naar login", () -> {
                currentUser = "Gast";
                currentRole = "GAST";
                showLoginScreen();
            }), 0, 2);
        }

        home.setTop(topSection);
        home.setCenter(grid);

        mainLayout.setCenter(home);
        applyTheme();
    }

    private VBox createEnhancedDashboardCard(String emoji, String title, String desc, Runnable action) {
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(300, 180);
        card.setStyle("-fx-background-color: " + (isDarkMode ? darkCard : lightCard) +
                "; -fx-background-radius: 15; -fx-padding: 25; -fx-cursor: hand;");
        card.setEffect(new DropShadow(15, Color.rgb(0, 0, 0, 0.1)));

        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font(45));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(isDarkMode ? Color.WHITE : Color.web("#2d2d3c"));

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", 13));
        descLabel.setTextFill(isDarkMode ? Color.LIGHTGRAY : Color.GRAY);
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(250);

        card.getChildren().addAll(emojiLabel, titleLabel, descLabel);

        // Hover animation
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-background-color: " + (isDarkMode ? "#1e3a5f" : "#e8f0ff") +
                    "; -fx-background-radius: 15; -fx-padding: 25; -fx-cursor: hand;");
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: " + (isDarkMode ? darkCard : lightCard) +
                    "; -fx-background-radius: 15; -fx-padding: 25; -fx-cursor: hand;");
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Klik‑actie
        card.setOnMouseClicked(e -> action.run());

        return card;
    }
    // ==================== AGENDA VIEW ====================
    // ==================== AGENDA VIEW ====================
    private void showAgendaView() {
        BorderPane agendaLayout = new BorderPane();
        agendaLayout.setPadding(new Insets(30));

        Label title = new Label("📅 Agenda - Weekoverzicht");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        String[] dagen = {"Maandag","Dinsdag","Woensdag","Donderdag","Vrijdag","Zaterdag","Zondag"};

        try {
            AgendaDAO dao = new AgendaDAO();
            int week = 1; // later dynamisch maken

            for (int i = 0; i < dagen.length; i++) {
                String dagNaam = dagen[i]; // ✅ effectively final

                Label dagLabel = new Label(dagNaam);
                dagLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                grid.add(dagLabel, i, 0);

                VBox dagBox = new VBox(10);
                dagBox.setPrefSize(150, 400);
                dagBox.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10; -fx-padding: 10;");

                // Bestaande items laden
                List<AgendaItem> items = dao.getAgendaItems(currentUserId, week, dagNaam);
                for (AgendaItem item : items) {
                    Label vakje = new Label(item.toString()); // gebruik toString()
                    vakje.setPrefSize(140, 50);
                    vakje.setAlignment(Pos.CENTER);
                    vakje.setStyle("-fx-background-color: #d6eaf8; -fx-border-color: #5dade2; "
                            + "-fx-background-radius: 5; -fx-border-radius: 5;");
                    dagBox.getChildren().add(vakje);
                }

                // Voeg lege vakjes toe
                for (int j = items.size(); j < 5; j++) {
                    Label vakje = new Label("Klik om te plannen");
                    vakje.setPrefSize(140, 50);
                    vakje.setAlignment(Pos.CENTER);
                    vakje.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; "
                            + "-fx-background-radius: 5; -fx-border-radius: 5;");

                    vakje.setOnMouseClicked(e -> {
                        // 1) Vraag tijd
                        TextInputDialog tijdDialog = new TextInputDialog();
                        tijdDialog.setTitle("Tijd invoeren");
                        tijdDialog.setHeaderText("Hoelaat wil je hieraan werken?");
                        tijdDialog.setContentText("Tijd (bijv. 10:00):");
                        Optional<String> tijdResult = tijdDialog.showAndWait();
                        if (tijdResult.isEmpty()) return;
                        String tijd = tijdResult.get().trim();
                        if (tijd.isEmpty()) return;

                        // 2) Vraag activiteit
                        TextInputDialog activiteitDialog = new TextInputDialog();
                        activiteitDialog.setTitle("Activiteit invoeren");
                        activiteitDialog.setHeaderText("Wat wil je gaan doen?");
                        activiteitDialog.setContentText("Activiteit:");
                        Optional<String> activiteitResult = activiteitDialog.showAndWait();
                        if (activiteitResult.isEmpty()) return;
                        String activiteit = activiteitResult.get().trim();
                        if (activiteit.isEmpty()) return;

                        // 3) Dropdown voor werkduur
                        List<String> opties = Arrays.asList(
                                "0:30 minuten", "1 uur", "1 uur 30 minuten",
                                "2 uur", "2 uur 30 minuten", "3 uur",
                                "3 uur 30 minuten", "4 uur", "4 uur 30 minuten",
                                "5 uur", "5 uur 30 minuten", "6 uur"
                        );
                        ChoiceDialog<String> duurDialog = new ChoiceDialog<>(opties.get(0), opties);
                        duurDialog.setTitle("Werkduur kiezen");
                        duurDialog.setHeaderText("Hoelang wil je hieraan werken?");
                        duurDialog.setContentText("Kies tijd:");
                        Optional<String> duurResult = duurDialog.showAndWait();
                        if (duurResult.isEmpty()) return;
                        String duur = duurResult.get();

                        // UI update
                        String labelText = tijd + " — " + activiteit + " (" + duur + ")";
                        vakje.setText(labelText);
                        vakje.setStyle("-fx-background-color: #d6eaf8; -fx-border-color: #5dade2; "
                                + "-fx-background-radius: 5; -fx-border-radius: 5;");

                        // Opslaan in DB
                        try {
                            dao.saveAgendaItem(currentUserId, week, dagNaam, tijd, activiteit, duur);
                        } catch (Exception ex) {
                            showStyledAlert(Alert.AlertType.ERROR, "Agenda fout", "Opslaan mislukt: " + ex.getMessage());
                        }

                        // ✅ Meld student dat de Educlock zal afgaan
                        showStyledAlert(Alert.AlertType.INFORMATION, "Nog te doen",
                                "De opdracht is verplaatst naar Nog te doen.\nDe Educlock zal om "
                                        + tijd + " aan gaan.");

                        // Navigatie naar opdrachten-tabblad
                        showOpdrachtenView();
                    });

                    dagBox.getChildren().add(vakje);
                }

                grid.add(dagBox, i, 1);
            }
        } catch (Exception e) {
            showStyledAlert(Alert.AlertType.ERROR, "Agenda fout", "Kon agenda niet laden: " + e.getMessage());
        }

        agendaLayout.setTop(title);
        agendaLayout.setCenter(grid);
        mainLayout.setCenter(agendaLayout);
    }

    //-------------OPDRACHTEN VIEW---------------

    private void showOpdrachtenView() {
        BorderPane opdrachtenLayout = new BorderPane();
        opdrachtenLayout.setPadding(new Insets(30));

        Label title = new Label("📝 Opdrachten");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        // Drie kolommen naast elkaar
        HBox columns = new HBox(80);
        columns.setAlignment(Pos.TOP_CENTER);

        // Linker kolom: Nog te doen
        VBox nogTeDoenBox = new VBox(15);
        nogTeDoenBox.setAlignment(Pos.TOP_LEFT);
        Label nogTeDoenLabel = new Label("Nog te doen");
        nogTeDoenLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        nogTeDoenBox.getChildren().add(nogTeDoenLabel);

        // Midden kolom: Afgerond
        VBox afgerondBox = new VBox(15);
        afgerondBox.setAlignment(Pos.TOP_LEFT);
        Label afgerondLabel = new Label("Afgerond");
        afgerondLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        afgerondBox.getChildren().add(afgerondLabel);

        // Rechter kolom: Nagekeken door docent
        VBox nagekekenBox = new VBox(15);
        nagekekenBox.setAlignment(Pos.TOP_LEFT);
        Label nagekekenLabel = new Label("Nagekeken door docent");
        nagekekenLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        nagekekenBox.getChildren().add(nagekekenLabel);

        try {
            AgendaDAO dao = new AgendaDAO();
            List<AgendaItem> items = dao.getAllOpdrachten();

            for (AgendaItem item : items) {
                BorderPane card = new BorderPane();
                card.setPadding(new Insets(10));
                card.setStyle("-fx-border-color: #cccccc; -fx-border-radius: 6; "
                        + "-fx-background-color: #ffffff; -fx-background-radius: 6;");

                VBox infoBox = new VBox(6);
                infoBox.setPadding(new Insets(6));
                Label dagLabel = new Label("Dag: " + item.getDag());
                Label tijdLabel = new Label("Tijd: " + item.getTijd());
                Label activiteitLabel = new Label("Activiteit: " + item.getActiviteit());
                Label duurLabel = new Label("Werkduur: " + item.getWerkduur());
                Label statusLabel = new Label("Status: " + item.getStatus());
                infoBox.getChildren().addAll(dagLabel, tijdLabel, activiteitLabel, duurLabel, statusLabel);

                // 📄 Bewijs uploaden-knop
                Button bewijsBtn = new Button("Bewijs uploaden");
                bewijsBtn.setStyle(
                        "-fx-background-color: #2196F3;" +     // modern blauw
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 5 8;"
                );
                HBox.setMargin(bewijsBtn, new Insets(0, 18, 0, 0));

// Hover-effect
                bewijsBtn.setOnMouseEntered(e ->
                        bewijsBtn.setStyle(
                                "-fx-background-color: #1976D2;" + // donkerder blauw bij hover
                                        "-fx-text-fill: white;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 6;" +
                                        "-fx-padding: 5 8;"
                        )
                );
                HBox.setMargin(bewijsBtn, new Insets(0, 18, 0, 0));

                bewijsBtn.setOnMouseExited(e ->
                        bewijsBtn.setStyle(
                                "-fx-background-color: #2196F3;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 6;" +
                                        "-fx-padding: 5 8;"
                        )
                );

                bewijsBtn.setOnAction(ev -> {
                    showUploadBewijsView(item);
                });

                // ❌ Delete-knop
                Button deleteBtn = new Button("✖");
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
                deleteBtn.setOnAction(e -> {
                    try {
                        dao.deleteAgendaItem(item.getId());
                        nogTeDoenBox.getChildren().remove(card);
                        afgerondBox.getChildren().remove(card);
                        nagekekenBox.getChildren().remove(card);
                        showStyledAlert(Alert.AlertType.INFORMATION, "Verwijderd",
                                "Opdracht is verwijderd uit de agenda.");
                    } catch (SQLException ex) {
                        showStyledAlert(Alert.AlertType.ERROR, "Database fout",
                                "Kon opdracht niet verwijderen: " + ex.getMessage());
                    }
                });

                // ✔ Afgerond-knop
                Button doneBtn = new Button("✔");
                doneBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: green;");
                doneBtn.setOnAction(e -> {
                    try {
                        dao.markAgendaItemAfgerond(item.getId());
                        item.setStatus("Afgerond");

                        // verplaats kaart naar Afgerond
                        nogTeDoenBox.getChildren().remove(card);
                        if (!afgerondBox.getChildren().contains(card)) {
                            afgerondBox.getChildren().add(card);
                        }

                        // update statuslabel
                        statusLabel.setText("Status: " + item.getStatus());

                        // ✅ zet de actionBox opnieuw zodat alleen bewijsBtn zichtbaar is
                        HBox newActionBox = new HBox(8, bewijsBtn);
                        newActionBox.setAlignment(Pos.TOP_RIGHT);
                        card.setTop(newActionBox);

                        showStyledAlert(Alert.AlertType.INFORMATION, "Afgerond",
                                "De opdracht is verplaatst naar Afgerond.");
                    } catch (SQLException ex) {
                        showStyledAlert(Alert.AlertType.ERROR, "Database fout",
                                "Kon opdracht niet afronden: " + ex.getMessage());
                    }
                });

                // ✅ Bekijk feedback-knop

                Button checkFeedbackBtn = new Button("Bekijk feedback");
                checkFeedbackBtn.setStyle(
                        "-fx-background-color: #4CAF50;" +     // groen
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 6;" +
                                "-fx-padding: 5 8;"
                );

// Hover-effect
                checkFeedbackBtn.setOnMouseEntered(e ->
                        checkFeedbackBtn.setStyle(
                                "-fx-background-color: #45a049;" + // donkerder groen bij hover
                                        "-fx-text-fill: white;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 6;" +
                                        "-fx-padding: 5 8;"
                        )
                );
                checkFeedbackBtn.setOnMouseExited(e ->
                        checkFeedbackBtn.setStyle(
                                "-fx-background-color: #4CAF50;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-background-radius: 6;" +
                                        "-fx-padding: 5 8;"
                        )
                );

                checkFeedbackBtn.setOnAction(ev -> {
                    // 🔹 Custom popup in plaats van standaard Alert
                    Stage popupStage = new Stage();
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    popupStage.setTitle("Feedback van docent");

                    VBox layout = new VBox(15);
                    layout.setPadding(new Insets(20));
                    layout.setAlignment(Pos.TOP_LEFT);
                    layout.setStyle(
                            "-fx-background-color: #f9f9f9;" +
                                    "-fx-border-color: #4CAF50;" +       // groene rand
                                    "-fx-border-width: 2;" +
                                    "-fx-background-radius: 10;" +
                                    "-fx-border-radius: 10;"
                    );

                    Label headerLabel = new Label("Je feedback:");
                    headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");

                    Label feedbackLabel = new Label(item.getFeedback());
                    feedbackLabel.setWrapText(true);
                    feedbackLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

                    Button closeBtn = new Button("Sluiten");
                    closeBtn.setStyle(
                            "-fx-background-color: #4CAF50;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 6;" +
                                    "-fx-padding: 6 14;"
                    );
                    closeBtn.setOnMouseEntered(e ->
                            closeBtn.setStyle(
                                    "-fx-background-color: #45a049;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-background-radius: 6;" +
                                            "-fx-padding: 6 14;"
                            )
                    );
                    closeBtn.setOnMouseExited(e ->
                            closeBtn.setStyle(
                                    "-fx-background-color: #4CAF50;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-background-radius: 6;" +
                                            "-fx-padding: 6 14;"
                            )
                    );
                    closeBtn.setOnAction(e -> popupStage.close());

                    layout.getChildren().addAll(headerLabel, feedbackLabel, closeBtn);

                    Scene scene = new Scene(layout, 400, 200);
                    popupStage.setScene(scene);
                    popupStage.showAndWait();
                });
                // Acties afhankelijk van status
                HBox actionBox = new HBox(8);
                actionBox.setAlignment(Pos.TOP_RIGHT);

                if (item.isNogTeDoen()) {
                    actionBox.getChildren().addAll(deleteBtn, doneBtn);
                } else if ("Afgerond".equalsIgnoreCase(item.getStatus())) {
                    actionBox.getChildren().addAll(bewijsBtn);
                } else if ("Nagekeken".equalsIgnoreCase(item.getStatus())) {
                    actionBox.getChildren().add(deleteBtn);

                    if (item.getFeedback() != null && !item.getFeedback().isBlank()) {
                        actionBox.getChildren().add(checkFeedbackBtn);
                    } else {
                        // ✅ Geen feedback → toon label onderaan
                        Label wachtenLabel = new Label("Geen feedback");
                        wachtenLabel.setStyle("-fx-text-fill: #8B0000; -fx-font-weight: bold;");
                        card.setBottom(wachtenLabel);
                    }
                }

                card.setTop(actionBox);
                card.setCenter(infoBox);

                // Plaats in juiste kolom
                if (item.isNogTeDoen()) {
                    nogTeDoenBox.getChildren().add(card);
                } else if ("Afgerond".equalsIgnoreCase(item.getStatus())) {
                    afgerondBox.getChildren().add(card);
                } else if ("Nagekeken".equalsIgnoreCase(item.getStatus())) {
                    nagekekenBox.getChildren().add(card);
                }
            }
        } catch (SQLException ex) {
            showStyledAlert(Alert.AlertType.ERROR, "Database fout",
                    "Kon opdrachten niet laden: " + ex.getMessage());
        }

        columns.getChildren().addAll(nogTeDoenBox, afgerondBox, nagekekenBox);

        ScrollPane scrollPane = new ScrollPane(columns);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        opdrachtenLayout.setTop(title);
        BorderPane.setAlignment(title, Pos.CENTER);
        opdrachtenLayout.setCenter(scrollPane);

        mainLayout.setCenter(opdrachtenLayout);
    }

    // ==================== UPLOAD BEWIJS VIEW ====================

    private void showUploadBewijsView(AgendaItem item) {
        BorderPane bewijsLayout = new BorderPane();
        bewijsLayout.setPadding(new Insets(30));

        Label title = new Label("📤 Bewijs uploaden voor: " + item.getActiviteit());
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        // ✅ Dropdown met docenten uit DB
        Label docentLabel = new Label("Kies docent:");
        ComboBox<Docent> docentCombo = new ComboBox<>();
        docentCombo.setPromptText("Selecteer docent");

        try {
            DocentDAO docentDao = new DocentDAO();
            docentCombo.getItems().addAll(docentDao.getAllDocenten()); // lijst van Docent objecten
        } catch (Exception ex) {
            showStyledAlert(Alert.AlertType.ERROR, "Database fout", "Kon docenten niet laden: " + ex.getMessage());
        }

        // Bestand kiezen
        TextField bewijsPathField = new TextField();
        bewijsPathField.setPromptText("Geen bestand gekozen");
        bewijsPathField.setEditable(false);

        Button chooseFileBtn = new Button("Kies bestand...");
        chooseFileBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Kies bewijsbestand");
            File file = fc.showOpenDialog(((Node) e.getSource()).getScene().getWindow());
            if (file != null) {
                bewijsPathField.setText(file.getAbsolutePath());
            }
        });

        // ✅ Uploadknop met try/catch
        Button uploadBtn = new Button("Upload bewijs");
        uploadBtn.setOnAction(e -> {
            Docent gekozenDocent = docentCombo.getValue();
            String path = bewijsPathField.getText();

            if (gekozenDocent == null) {
                showStyledAlert(Alert.AlertType.ERROR, "Fout", "Kies eerst een docent.");
                return;
            }
            if (path == null || path.isBlank()) {
                showStyledAlert(Alert.AlertType.ERROR, "Fout", "Kies een bestand.");
                return;
            }

            try {
                BewijsDAO dao = new BewijsDAO();
                dao.saveBewijs(item.getId(), gekozenDocent.getId(), path);

                showStyledAlert(Alert.AlertType.INFORMATION, "Succes",
                        "Bewijs geüpload en verstuurd naar " + gekozenDocent.getNaam());

                // ✅ Terug naar overzicht
                showOpdrachtenView();
            } catch (Exception ex) {
                showStyledAlert(Alert.AlertType.ERROR, "Database fout", "Opslaan mislukt: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Layout
        grid.add(docentLabel, 0, 0);
        grid.add(docentCombo, 1, 0);
        grid.add(bewijsPathField, 0, 1);
        grid.add(chooseFileBtn, 1, 1);
        grid.add(uploadBtn, 1, 2);

        bewijsLayout.setTop(title);
        bewijsLayout.setCenter(grid);
        mainLayout.setCenter(bewijsLayout);
    }
    // ==================== 4. BEOORDELING VIEW ====================
    private void showBeoordelingView() {
        if (currentDocent != null) {
            showBeoordelingView(currentDocent.getId()); // roep de versie met parameter aan
        } else {
            showStyledAlert(Alert.AlertType.ERROR, "Geen docent", "Geen docent ingelogd!");
        }
    }

    private void showBeoordelingView(int currentDocentId) {
        BorderPane beoordelingLayout = new BorderPane();

        // 🔝 Top bar met titel en filter
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(12)); // compacter
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);

        Label title = new Label("Beoordeling");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        HBox.setHgrow(title, Priority.ALWAYS);

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("-select-", "Verstuurd", "Afgerond", "Nog te doen", "Nagekeken");
        filterCombo.setValue("-select-");

        topBar.getChildren().addAll(title, new Label("Filter:"), filterCombo);
        beoordelingLayout.setTop(topBar);

        // ✅ Compacte lijst van studentrijen
        VBox studentsList = new VBox(2); // minimale ruimte tussen rijen
        studentsList.setPadding(new Insets(8)); // minder buitenruimte
        studentsList.setStyle("-fx-background-color: transparent;"); // geen extra achtergrond

        try {
            BewijsDAO dao = new BewijsDAO();
            List<AgendaItem> uploads = dao.getBewijsVoorDocent(currentDocentId);

            // filter listener
            filterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                studentsList.getChildren().clear();
                for (AgendaItem fItem : uploads) {
                    if (newVal.equals("-select-") || fItem.getStatus().equalsIgnoreCase(newVal)) {
                        addStudentRow(studentsList, fItem);
                    }
                }
            });

            // standaard toevoegen
            for (AgendaItem item : uploads) {
                addStudentRow(studentsList, item);
            }
        } catch (Exception ex) {
            showStyledAlert(Alert.AlertType.ERROR, "Database fout", "Kon bewijs niet laden: " + ex.getMessage());
        }

        // ✅ Scrollpane zonder extra achtergrond
        ScrollPane scroll = new ScrollPane(studentsList);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        beoordelingLayout.setCenter(scroll);

        mainLayout.setCenter(beoordelingLayout);
        applyTheme();
    }
    private void addStudentRow(VBox container, AgendaItem item) {
        VBox row = new VBox(5);

        HBox mainRow = new HBox(15);
        mainRow.setPadding(new Insets(12));
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-cursor: hand;");

        Label nameLabel = new Label(item.getStudentName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label statusLabel = new Label(" " + item.getStatus() + " ");
        statusLabel.setStyle(getStatusStyle(item.getStatus()));

        Button toggleBtn = new Button("▼");
        toggleBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent;");

        mainRow.getChildren().addAll(nameLabel, statusLabel, toggleBtn);

        VBox details = new VBox(8);
        details.setPadding(new Insets(15, 15, 15, 40));
        details.setStyle("-fx-background-color: #f0f8ff;");
        details.setVisible(false);

        // ✅ Extra info over de opdracht
        details.getChildren().addAll(
                new Label("Activiteit: " + item.getActiviteit()),
                new Label("Dag: " + item.getDag()),
                new Label("Tijd: " + item.getTijd()),
                new Label("Werkduur: " + item.getWerkduur())
        );

        // ✅ bestandlink tonen en openen
        if (item.getBewijsPath() != null && !item.getBewijsPath().isBlank()) {
            Hyperlink bewijsLink = new Hyperlink(item.getBewijsPath());
            bewijsLink.setOnAction(e -> {
                try {
                    File file = new File(item.getBewijsPath());
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        showStyledAlert(Alert.AlertType.ERROR, "Bestand niet gevonden", "Het bestand bestaat niet: " + item.getBewijsPath());
                    }
                } catch (Exception ex) {
                    showStyledAlert(Alert.AlertType.ERROR, "Fout bij openen", "Kon bestand niet openen: " + ex.getMessage());
                }
            });
            details.getChildren().add(bewijsLink);
        }

        // ✅ feedbackveld
        HBox feedbackBox = new HBox(8);
        TextField feedbackField = new TextField();
        feedbackField.setPromptText("Geef feedback...");
        feedbackField.setPrefWidth(300);
        Button sendBtn = new Button("Verstuur");
        sendBtn.setStyle("-fx-background-color: #008cd7; -fx-text-fill: white;");
        sendBtn.setOnAction(e -> {
            String feedbackText = feedbackField.getText().trim();
            if (!feedbackText.isEmpty()) {
                try {
                    BewijsDAO dao = new BewijsDAO();
                    dao.saveFeedback(item.getId(), feedbackText);
                    item.setFeedback(feedbackText);
                    showStyledAlert(Alert.AlertType.INFORMATION,
                            "Feedback verstuurd",
                            "Feedback verstuurd naar " + item.getStudentName());
                    feedbackField.clear();
                } catch (Exception ex) {
                    showStyledAlert(Alert.AlertType.ERROR,
                            "Database fout",
                            "Kon feedback niet opslaan: " + ex.getMessage());
                }
            }
        });
        feedbackBox.getChildren().addAll(feedbackField, sendBtn);
        details.getChildren().add(feedbackBox);

        // ✅ Nagekeken-knop met rode melding en checkbox
        Button nagekekenBtn = new Button("Nagekeken");
        nagekekenBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        nagekekenBtn.setOnAction(e -> {
            // Eerst checken of docent de melding wil overslaan
            if (DocentPreferences.skipNagekekenWarning(currentDocent.getId())) {
                try {
                    BewijsDAO dao = new BewijsDAO();
                    dao.markBewijsNagekeken(item.getId());
                    item.setStatus("Nagekeken");
                    statusLabel.setText(" Nagekeken ");
                    statusLabel.setStyle(getStatusStyle("Nagekeken"));
                    showStyledAlert(Alert.AlertType.INFORMATION, "Nagekeken",
                            "De opdracht van " + item.getStudentName() + " is nagekeken.");
                } catch (Exception ex) {
                    showStyledAlert(Alert.AlertType.ERROR, "Database fout",
                            "Kon opdracht niet markeren als nagekeken: " + ex.getMessage());
                }
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Bevestig beoordeling");
            confirmAlert.setHeaderText("⚠️ Let op: belangrijke actie");

            Label message = new Label(
                    "Wanneer je deze opdracht als 'Nagekeken' markeert:\n\n" +
                            "• De bewijsupload van de student wordt permanent verwijderd!\n" +
                            "• Je kunt dan ook geen feedback meer verzenden!"

            );
            message.setWrapText(true);

            CheckBox dontShowAgain = new CheckBox("Laat deze melding niet meer zien");
            VBox content = new VBox(12, message, dontShowAgain);
            content.setPadding(new Insets(10));
            confirmAlert.getDialogPane().setContent(content);

            // 🔥 Styling
            DialogPane dialogPane = confirmAlert.getDialogPane();
            dialogPane.setStyle(
                    "-fx-background-color: #ffe6e6;" +
                            "-fx-border-color: #cc0000;" +
                            "-fx-border-width: 2px;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;"
            );
            Label header = (Label) dialogPane.lookup(".header-panel .label");
            if (header != null) {
                header.setStyle("-fx-text-fill: #cc0000; -fx-font-size: 16px; -fx-font-weight: bold;");
            }

            ButtonType jaBtn = new ButtonType("✅ Ja, markeer als nagekeken", ButtonBar.ButtonData.OK_DONE);
            ButtonType neeBtn = new ButtonType("❌ Annuleer", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(jaBtn, neeBtn);

            Button jaButton = (Button) dialogPane.lookupButton(jaBtn);
            jaButton.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white; -fx-font-weight: bold;");
            Button neeButton = (Button) dialogPane.lookupButton(neeBtn);
            neeButton.setStyle("-fx-background-color: #f2f2f2; -fx-text-fill: black;");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == jaBtn) {
                    try {
                        BewijsDAO dao = new BewijsDAO();
                        dao.markBewijsNagekeken(item.getId());
                        item.setStatus("Nagekeken");
                        statusLabel.setText(" Nagekeken ");
                        statusLabel.setStyle(getStatusStyle("Nagekeken"));
                        showStyledAlert(Alert.AlertType.INFORMATION, "Nagekeken",
                                "De opdracht van " + item.getStudentName() + " is nagekeken en de bewijsupload is verwijderd.");

                        if (dontShowAgain.isSelected()) {
                            DocentPreferences.setSkipNagekekenWarning(currentDocent.getId(), true);
                        }
                    } catch (Exception ex) {
                        showStyledAlert(Alert.AlertType.ERROR, "Database fout",
                                "Kon opdracht niet markeren als nagekeken: " + ex.getMessage());
                    }
                }
            });
        });

        details.getChildren().add(nagekekenBtn);

        toggleBtn.setOnAction(e -> {
            details.setVisible(!details.isVisible());
            toggleBtn.setText(details.isVisible() ? "▲" : "▼");
        });

        row.getChildren().addAll(mainRow, details);
        container.getChildren().add(row);
    }

    // Helper: geef CSS-stijl terug op basis van status
    private String getStatusStyle(String status) {
        switch (status.toUpperCase()) {
            case "VERSTUURD":
                return "-fx-background-color: #cce5ff; -fx-text-fill: #005aff; "
                        + "-fx-padding: 4 10; -fx-background-radius: 4;";
            case "AFGEROND":
                return "-fx-background-color: #ccffcc; -fx-text-fill: #008200; "
                        + "-fx-padding: 4 10; -fx-background-radius: 4;";
            case "NOG TE DOEN":
                return "-fx-background-color: #ffcccc; -fx-text-fill: #c80000; "
                        + "-fx-padding: 4 10; -fx-background-radius: 4;";
            case "NAGEKEKEN":
                return "-fx-background-color: #d0ffd0; -fx-text-fill: #006600; "
                        + "-fx-padding: 4 10; -fx-background-radius: 4;";
            default:
                return "-fx-background-color: #e0e0e0; "
                        + "-fx-padding: 4 10; -fx-background-radius: 4;";
        }
    }
    // ==================== ENHANCED SETTINGS ====================
    private void showSettings() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        VBox settings = new VBox(25);
        settings.setPadding(new Insets(40));
        settings.setMaxWidth(800);
        settings.setAlignment(Pos.TOP_CENTER);

        Label title = new Label("⚙️ Instellingen");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Separator sep1 = new Separator();

        // Profile section
        VBox profileSection = createSettingsSection("👤 Profiel",
                createSettingRow("Gebruiker:", currentUser, "Naam wijzigen", () -> changeUsername())
        );

        // Appearance section
        VBox appearanceSection = createSettingsSection("🎨 Weergave",
                createDarkModeToggle(),
                createFontSizeControl(),
                createLanguageSelector()
        );

        // Notifications section
        VBox notificationSection = createSettingsSection("🔔 Meldingen",
                createNotificationToggle("Email meldingen", true),
                createNotificationToggle("Push meldingen", false),
                createNotificationToggle("Agenda herinneringen", true)
        );

        // Eduklok section
        VBox eduklokSection = createSettingsSection("⏰ Eduklok Instellingen",
                createEduklokToggle(),
                createTimerPresets(),
                createLessonScheduleButton()
        );

        // Privacy section
        VBox privacySection = createSettingsSection("🔒 Privacy & Veiligheid",
                createPrivacyOption("Profiel zichtbaarheid", "Alleen docenten"),
                createPrivacyOption("Data delen", "Uit")
        );

        // Danger zone
        VBox dangerZone = createDangerZone();

        settings.getChildren().addAll(
                title, sep1,
                profileSection,
                appearanceSection,
                notificationSection,
                eduklokSection,
                privacySection,
                dangerZone
        );

        scrollPane.setContent(settings);
        mainLayout.setCenter(scrollPane);
        applyTheme();
    }

    private VBox createSettingsSection(String sectionTitle, Region... items) {
        VBox section = new VBox(15);
        section.setStyle("-fx-background-color: " + (isDarkMode ? darkCard : lightCard) +
                "; -fx-background-radius: 10; -fx-padding: 25;");
        section.setMaxWidth(750);

        Label title = new Label(sectionTitle);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        title.setTextFill(isDarkMode ? Color.WHITE : Color.web("#2d2d3c"));

        Separator sep = new Separator();

        section.getChildren().add(title);
        section.getChildren().add(sep);
        section.getChildren().addAll(items);

        return section;
    }

    private HBox createSettingRow(String label, String value, String buttonText, Runnable action) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        lbl.setPrefWidth(150);

        Label val = new Label(value);
        val.setFont(Font.font("Arial", 14));
        val.setTextFill(isDarkMode ? Color.LIGHTGRAY : Color.GRAY);
        HBox.setHgrow(val, Priority.ALWAYS);

        Button btn = new Button(buttonText);
        btn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        btn.setOnAction(e -> action.run());

        row.getChildren().addAll(lbl, val, btn);
        return row;
    }

    private HBox createDarkModeToggle() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label("🌙 Dark Mode");
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        CheckBox toggle = new CheckBox();
        toggle.setSelected(isDarkMode);
        toggle.setOnAction(e -> {
            isDarkMode = toggle.isSelected();
            applyTheme();
            showSettings(); // Refresh settings view
        });

        row.getChildren().addAll(lbl, toggle);
        return row;
    }

    private HBox createFontSizeControl() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label("🔤 Tekstgrootte");
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        lbl.setPrefWidth(150);

        ComboBox<String> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll("Klein", "Normaal", "Groot", "Extra groot");
        sizeBox.setValue("Normaal");
        sizeBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2d2d3c" : "white") + ";");

        row.getChildren().addAll(lbl, sizeBox);
        return row;
    }

    private HBox createLanguageSelector() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label("🌍 Taal");
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        lbl.setPrefWidth(150);

        ComboBox<String> langBox = new ComboBox<>();
        langBox.getItems().addAll("Nederlands", "English", "Deutsch", "Français", "العربية");
        langBox.setValue("Nederlands");
        langBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2d2d3c" : "white") + ";");

        row.getChildren().addAll(lbl, langBox);
        return row;
    }

    private HBox createNotificationToggle(String text, boolean defaultValue) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", 14));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        CheckBox toggle = new CheckBox();
        toggle.setSelected(defaultValue);

        row.getChildren().addAll(lbl, toggle);
        return row;
    }

    private HBox createEduklokToggle() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label("⏰ Eduklok inschakelen");
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        CheckBox toggle = new CheckBox();
        toggle.setSelected(true);
        toggle.setOnAction(e -> {
            if (toggle.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Eduklok is nu actief! ⏰");
                alert.showAndWait();
            }
        });

        row.getChildren().addAll(lbl, toggle);
        return row;
    }

    private HBox createTimerPresets() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label("⏱️ Standaard timer");
        lbl.setFont(Font.font("Arial", 14));
        lbl.setPrefWidth(150);

        ComboBox<String> timerBox = new ComboBox<>();
        timerBox.getItems().addAll("25 min (Pomodoro)", "45 min", "1 uur", "1.5 uur", "2 uur");
        timerBox.setValue("45 min");
        timerBox.setStyle("-fx-background-color: " + (isDarkMode ? "#2d2d3c" : "white") + ";");

        row.getChildren().addAll(lbl, timerBox);
        return row;
    }

    private HBox createLessonScheduleButton() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label("📚 Lesrooster");
        lbl.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 14));
        HBox.setHgrow(lbl, Priority.ALWAYS);

        Button editBtn = new Button("Bewerken");
        editBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-padding: 8 15; -fx-background-radius: 5; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showLessonScheduleEditor());

        row.getChildren().addAll(lbl, editBtn);
        return row;
    }

    private void showLessonScheduleEditor() {
        Stage scheduleStage = new Stage();
        scheduleStage.setTitle("Lesrooster bewerken");

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + (isDarkMode ? darkBg : lightBg) + ";");

        Label title = new Label("📚 Bewerk je lesrooster");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(isDarkMode ? Color.WHITE : Color.web("#2d2d3c"));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        VBox lessonsList = new VBox(10);
        lessonsList.setPadding(new Insets(10));

        // Display existing lessons
        for (int i = 0; i < lessonSchedule.size(); i++) {
            String[] lesson = lessonSchedule.get(i);
            HBox lessonRow = createLessonRow(lesson, i, lessonsList);
            lessonsList.getChildren().add(lessonRow);
        }

        scrollPane.setContent(lessonsList);

        // Add new lesson button
        Button addBtn = new Button("+ Nieuw les toevoegen");
        addBtn.setStyle("-fx-background-color: #5a9f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            String[] newLesson = {"09:00", "10:00", "Nieuw vak"};
            lessonSchedule.add(newLesson);
            lessonsList.getChildren().add(createLessonRow(newLesson, lessonSchedule.size() - 1, lessonsList));
        });

        // Save button
        Button saveBtn = new Button("Opslaan");
        saveBtn.setStyle("-fx-background-color: #667eea; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        saveBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Lesrooster opgeslagen! ✅");
            alert.showAndWait();
            scheduleStage.close();
        });

        HBox buttons = new HBox(10);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(addBtn, saveBtn);

        root.getChildren().addAll(title, new Separator(), scrollPane, buttons);

        Scene scene = new Scene(root, 700, 600);
        scheduleStage.setScene(scene);
        scheduleStage.show();
    }

    private HBox createLessonRow(String[] lesson, int index, VBox container) {
        HBox row = new HBox(10);
        row.setPadding(new Insets(10));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + (isDarkMode ? darkCard : "white") +
                "; -fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8;");

        // Start time
        TextField startField = new TextField(lesson[0]);
        startField.setPrefWidth(80);
        startField.setPromptText("HH:MM");
        startField.setStyle("-fx-font-size: 13;");
        startField.textProperty().addListener((obs, old, newVal) -> {
            lesson[0] = newVal;
        });

        Label dash = new Label("—");
        dash.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // End time
        TextField endField = new TextField(lesson[1]);
        endField.setPrefWidth(80);
        endField.setPromptText("HH:MM");
        endField.setStyle("-fx-font-size: 13;");
        endField.textProperty().addListener((obs, old, newVal) -> {
            lesson[1] = newVal;
        });

        // Subject name
        TextField subjectField = new TextField(lesson[2]);
        subjectField.setPrefWidth(200);
        subjectField.setPromptText("Vak naam");
        subjectField.setStyle("-fx-font-size: 13;");
        subjectField.textProperty().addListener((obs, old, newVal) -> {
            lesson[2] = newVal;
        });

        HBox.setHgrow(subjectField, Priority.ALWAYS);

        // Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: #c44545; -fx-text-fill: white; -fx-font-size: 16; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Weet je zeker dat je deze les wilt verwijderen?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    lessonSchedule.remove(index);
                    container.getChildren().remove(row);
                }
            });
        });

        row.getChildren().addAll(startField, dash, endField, new Label("•"), subjectField, deleteBtn);
        return row;
    }

    private HBox createPrivacyOption(String label, String value) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 0, 10, 0));

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", 14));
        lbl.setPrefWidth(200);

        ComboBox<String> combo = new ComboBox<>();
        if (label.contains("zichtbaarheid")) {
            combo.getItems().addAll("Publiek", "Alleen docenten", "Privé");
        } else {
            combo.getItems().addAll("Aan", "Uit");
        }
        combo.setValue(value);
        combo.setStyle("-fx-background-color: " + (isDarkMode ? "#2d2d3c" : "white") + ";");

        row.getChildren().addAll(lbl, combo);
        return row;
    }

    private VBox createDangerZone() {
        VBox zone = new VBox(15);
        zone.setStyle("-fx-background-color: #ffe6e6; -fx-background-radius: 10; -fx-padding: 25; -fx-border-color: #ff4444; -fx-border-width: 2; -fx-border-radius: 10;");
        zone.setMaxWidth(750);

        Label title = new Label("⚠️ Danger Zone");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        title.setTextFill(Color.web("#cc0000"));

        Button deleteBtn = new Button("Account verwijderen");
        deleteBtn.setStyle("-fx-background-color: #cc0000; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Weet je zeker dat je je account wilt verwijderen?");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION, "Account verwijderd (simulatie)");
                    info.showAndWait();
                }
            });
        });

        zone.getChildren().addAll(title, deleteBtn);
        return zone;
    }

    private void changeUsername() {
        TextInputDialog dialog = new TextInputDialog(currentUser);
        dialog.setTitle("Naam wijzigen");
        dialog.setHeaderText("Voer je nieuwe naam in:");
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                currentUser = newName;
                ((Label)sidebar.getChildren().get(0)).setText("Gebruiker: " + currentUser);
                showSettings(); // Refresh
            }
        });
    }

    private void applyTheme() {
        String bgColor = isDarkMode ? darkBg : lightBg;
        String sidebarColor = isDarkMode ? darkSidebar : lightSidebar;
        String textColor = isDarkMode ? "#ffffff" : "#2d2d3c";

        if (mainLayout != null) {
            mainLayout.setStyle("-fx-background-color: " + bgColor + ";");
        }

        if (sidebar != null) {
            sidebar.setStyle("-fx-background-color: " + sidebarColor + ";");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
