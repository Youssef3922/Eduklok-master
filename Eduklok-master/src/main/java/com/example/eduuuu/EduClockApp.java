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




public class EduClockApp extends Application {
    private Stage primaryStage;
    private BorderPane mainLayout;
    private String currentUser = "Gast";
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
                currentUser = username;
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

                DocentDAO dao = new DocentDAO();
                String resultMessage = dao.checkLogin(username, password);

                if (resultMessage.contains("✅")) {
                    currentUser = username;
                    currentRole = "DOCENT"; // rol instellen
                    showMainApp();
                } else {
                    showStyledAlert(Alert.AlertType.ERROR, "Inloggen mislukt", resultMessage);
                }
            });

            Hyperlink registerLink = new Hyperlink("Nog geen docent account? Registreer hier");
            registerLink.setFont(Font.font("Arial", 12));
            registerLink.setTextFill(Color.web("#2193b0"));
            registerLink.setOnAction(ev -> showTeacherRegisterScreen());

            Hyperlink backToStudentLogin = new Hyperlink("Terug naar student login");
            backToStudentLogin.setFont(Font.font("Arial", 12));
            backToStudentLogin.setTextFill(Color.web("#2193b0"));
            backToStudentLogin.setOnAction(ev -> showLoginScreen());

            loginCard.getChildren().addAll(title, usernameField, passwordField, loginBtn, registerLink, backToStudentLogin);
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

            private void showMainApp() {
                mainLayout = new BorderPane();
                VBox sidebar = createSidebar();
                sidebar.setStyle("-fx-background-color: #2c2f4a;"); // vaste donkere kleur
                mainLayout.setLeft(sidebar);
                showHomeView(); // start met Home in het midden

                Scene scene = new Scene(mainLayout, 1200, 750);
                primaryStage.setScene(scene);
                primaryStage.show();
            }





    // ==================== SIDEBAR ====================
    private VBox createSidebar() {
        VBox sidebar = new VBox(15);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(30, 15, 30, 15));
        sidebar.setStyle("-fx-background-color: #2c2f4a;"); // vaste donkere kleur

        Label userLabel = new Label("Gebruiker: " + currentUser);
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Region spacer = new Region();
        spacer.setPrefHeight(20);

        if ("STUDENT".equalsIgnoreCase(currentRole)) {
            sidebar.getChildren().addAll(
                    userLabel, spacer,
                    createMenuButton("🏠 Home", () -> showHomeView()),
                    createMenuButton("📅 Agenda", () -> showAgendaView()),
                    createMenuButton("📝 Opdrachten", () -> showUploadView()),
                    createMenuButton("⚙️ Instellingen", () -> showSettings()),
                    createMenuButton("🚪 Uitloggen", () -> {
                        currentUser = "Gast";
                        currentRole = "GAST";
                        showLoginScreen();
                    })
            );
        } else if ("DOCENT".equalsIgnoreCase(currentRole)) {
            sidebar.getChildren().addAll(
                    userLabel, spacer,
                    createMenuButton("🏠 Home", () -> showHomeView()),
                    createMenuButton("👥 Leerling-Data", () -> showBeoordelingView()),
                    createMenuButton("⚙️ Instellingen", () -> showSettings()),
                    createMenuButton("🚪 Uitloggen", () -> {
                        currentUser = "Gast";
                        currentRole = "GAST";
                        showLoginScreen();
                    })
            );
        } else {
            sidebar.getChildren().addAll(
                    userLabel, spacer,
                    createMenuButton("🏠 Home", () -> showHomeView()),
                    createMenuButton("📅 Agenda", () -> showAgendaView()),
                    createMenuButton("📝 Opdrachten", () -> showUploadView()),
                    createMenuButton("👥 Leerling-Data", () -> showBeoordelingView()),
                    createMenuButton("⚙️ Instellingen", () -> showSettings()),
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
                + "-fx-padding: 12 20; -fx-background-radius: 5; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #5555aa; -fx-text-fill: white; -fx-font-size: 14; "
                        + "-fx-padding: 12 20; -fx-background-radius: 5; -fx-cursor: hand;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #46465a; -fx-text-fill: white; -fx-font-size: 14; "
                        + "-fx-padding: 12 20; -fx-background-radius: 5; -fx-cursor: hand;"
        ));

        btn.setOnAction(e -> action.run());
        return btn;
    }
    // ==================== ENHANCED HOME VIEW ====================
    private void showHomeView() {
        BorderPane home = new BorderPane();
        home.setPadding(new Insets(40));

        // Top section with welcome and clock
        VBox topSection = new VBox(30);
        topSection.setAlignment(Pos.CENTER);

        Label welcome = new Label("Welkom bij EduClock, " + currentUser + "! 👋");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        // Eduklok widget (from image)
        VBox eduklokWidget = createEduklokWidget();

        topSection.getChildren().addAll(welcome, eduklokWidget);

        // Dashboard cards
        GridPane grid = new GridPane();
        grid.setHgap(25);
        grid.setVgap(25);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(30, 0, 0, 0));

        grid.add(createEnhancedDashboardCard("📅", "Agenda", "Bekijk en beheer je rooster", () -> showAgendaView()), 0, 0);
        grid.add(createEnhancedDashboardCard("📝", "Opdrachten", "Upload je bewijs en opdrachten", () -> showUploadView()), 1, 0);
        grid.add(createEnhancedDashboardCard("👥", "Leerlingen", "Bekijk en geef beoordelingen", () -> showBeoordelingView()), 0, 1);
        grid.add(createEnhancedDashboardCard("⚙️", "Instellingen", "Pas je profiel en voorkeuren aan", () -> showSettings()), 1, 1);

        home.setTop(topSection);
        home.setCenter(grid);

        mainLayout.setCenter(home);
        applyTheme();
    }

    // Eduklok Widget (Clock from image)
    private VBox createEduklokWidget() {
        VBox widget = new VBox(15);
        widget.setAlignment(Pos.CENTER);
        widget.setPrefWidth(450);
        widget.setPrefHeight(200);
        widget.setStyle("-fx-background-color: " + (isDarkMode ? darkCard : lightCard) +
                "; -fx-background-radius: 20; -fx-padding: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5);");

        HBox mainContent = new HBox(30);
        mainContent.setAlignment(Pos.CENTER);

        // Left side - Current lesson time (Leer)
        VBox leftSide = new VBox(10);
        leftSide.setAlignment(Pos.CENTER);
        leftSide.setPrefWidth(180);
        leftSide.setStyle("-fx-background-color: #3d3d52; -fx-background-radius: 15; -fx-padding: 20;");

        Label leerLabel = new Label("Leer");
        leerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        leerLabel.setTextFill(Color.WHITE);

        Label lessonTimeLabel = new Label("--:--");
        lessonTimeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        lessonTimeLabel.setTextFill(Color.WHITE);

        Label lessonInfo = new Label("Wiskunde");
        lessonInfo.setFont(Font.font("Arial", 12));
        lessonInfo.setTextFill(Color.web("#b0b0c0"));

        // Control buttons
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Button startBtn = new Button("▶");
        startBtn.setPrefSize(35, 35);
        startBtn.setStyle("-fx-background-color: #5a9f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");

        Button pauseBtn = new Button("⏸");
        pauseBtn.setPrefSize(35, 35);
        pauseBtn.setStyle("-fx-background-color: #d68c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");

        Button stopBtn = new Button("⏹");
        stopBtn.setPrefSize(35, 35);
        stopBtn.setStyle("-fx-background-color: #c44545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");

        controls.getChildren().addAll(startBtn, pauseBtn, stopBtn);

        leftSide.getChildren().addAll(leerLabel, lessonTimeLabel, lessonInfo, controls);

        // Right side - Study duration (Leertijd)
        VBox rightSide = new VBox(10);
        rightSide.setAlignment(Pos.CENTER_LEFT);
        rightSide.setPrefWidth(180);

        Label leertijdLabel = new Label("LEERTIJD");
        leertijdLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        leertijdLabel.setTextFill(isDarkMode ? Color.LIGHTGRAY : Color.GRAY);

        Label studyDurationLabel = new Label("0:00:00");
        studyDurationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        studyDurationLabel.setTextFill(isDarkMode ? Color.WHITE : Color.web("#2d2d3c"));

        Label totalLabel = new Label("Vandaag: 2:15:30");
        totalLabel.setFont(Font.font("Arial", 11));
        totalLabel.setTextFill(isDarkMode ? Color.LIGHTGRAY : Color.GRAY);

        TextField questionField = new TextField();
        questionField.setPromptText("Typ je vraag...");
        questionField.setStyle("-fx-background-color: " + (isDarkMode ? "#2d2d3c" : "#f0f0f0") +
                "; -fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 12;");

        rightSide.getChildren().addAll(leertijdLabel, studyDurationLabel, totalLabel, questionField);

        mainContent.getChildren().addAll(leftSide, rightSide);
        widget.getChildren().add(mainContent);

        // Start timers
        startLessonClock(lessonTimeLabel);
        startStudyTimer(studyDurationLabel, startBtn, pauseBtn, stopBtn);

        return widget;
    }

    // Shows current lesson time or next lesson
    private void startLessonClock(Label lessonTimeLabel) {
        Timeline lessonClock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime now = LocalTime.now();

            String displayTime = "--:--";
            String lessonName = "Geen les";

            for (String[] lesson : lessonSchedule) {
                LocalTime start = LocalTime.parse(lesson[0]);
                LocalTime end = LocalTime.parse(lesson[1]);

                if (now.isAfter(start) && now.isBefore(end)) {
                    // Current lesson - show time remaining
                    long secondsLeft = java.time.Duration.between(now, end).getSeconds();
                    long hours = secondsLeft / 3600;
                    long minutes = (secondsLeft % 3600) / 60;
                    long seconds = secondsLeft % 60;
                    displayTime = String.format("%d:%02d:%02d", hours, minutes, seconds);
                    lessonName = lesson[2];
                    break;
                } else if (now.isBefore(start)) {
                    // Next lesson - show start time
                    displayTime = lesson[0];
                    lessonName = "Volgende: " + lesson[2];
                    break;
                }
            }

            lessonTimeLabel.setText(displayTime);
        }));
        lessonClock.setCycleCount(Timeline.INDEFINITE);
        lessonClock.play();
    }

    // Study timer (stopwatch)
    private void startStudyTimer(Label studyDurationLabel, Button startBtn, Button pauseBtn, Button stopBtn) {
        final long[] studySeconds = {0}; // Total study seconds
        final boolean[] isRunning = {false};
        final boolean[] isPaused = {false};

        Timeline studyTimer = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (isRunning[0] && !isPaused[0]) {
                studySeconds[0]++;
                long hours = studySeconds[0] / 3600;
                long minutes = (studySeconds[0] % 3600) / 60;
                long seconds = studySeconds[0] % 60;
                studyDurationLabel.setText(String.format("%d:%02d:%02d", hours, minutes, seconds));
            }
        }));
        studyTimer.setCycleCount(Timeline.INDEFINITE);

        // Start button
        startBtn.setOnAction(e -> {
            if (!isRunning[0]) {
                isRunning[0] = true;
                isPaused[0] = false;
                studyTimer.play();
                startBtn.setStyle("-fx-background-color: #4a7f4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");
            } else if (isPaused[0]) {
                isPaused[0] = false;
                startBtn.setStyle("-fx-background-color: #4a7f4a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");
            }
        });

        // Pause button
        pauseBtn.setOnAction(e -> {
            if (isRunning[0]) {
                isPaused[0] = !isPaused[0];
                if (isPaused[0]) {
                    pauseBtn.setStyle("-fx-background-color: #b67800; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");
                } else {
                    pauseBtn.setStyle("-fx-background-color: #d68c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");
                }
            }
        });

        // Stop button
        stopBtn.setOnAction(e -> {
            if (isRunning[0]) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Wil je de studietijd stoppen? Je hebt " + studyDurationLabel.getText() + " gestudeerd.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        isRunning[0] = false;
                        isPaused[0] = false;
                        studySeconds[0] = 0;
                        studyDurationLabel.setText("0:00:00");
                        startBtn.setStyle("-fx-background-color: #5a9f5a; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");
                        pauseBtn.setStyle("-fx-background-color: #d68c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 50%; -fx-cursor: hand;");
                    }
                });
            }
        });
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

        card.setOnMouseClicked(e -> action.run());

        return card;
    }

    private VBox createDashboardCard(String title, String desc, Runnable action) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(280, 160);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-cursor: hand;");
        card.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.1)));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", 14));
        descLabel.setTextFill(Color.GRAY);

        card.getChildren().addAll(titleLabel, descLabel);
        card.setOnMouseClicked(e -> action.run());
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #e8f0ff; -fx-background-radius: 10; -fx-padding: 20; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-cursor: hand;"));

        return card;
    }

    // ==================== AGENDA VIEW ====================
    private int huidigeWeek = 1;
    private int currentUserId = -1; // wordt gezet bij login

    private void showAgendaView() {
        VBox agendaWrapper = new VBox(20);
        agendaWrapper.setPadding(new Insets(20));
        agendaWrapper.setStyle("-fx-background-color: #f4f4f4;");

        HBox weekSelector = new HBox(10);
        weekSelector.setAlignment(Pos.CENTER_LEFT);

        Label weekLabel = new Label("Week:");
        weekLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        ComboBox<Integer> weekDropdown = new ComboBox<>();
        for (int i = 1; i <= 52; i++) {
            weekDropdown.getItems().add(i);
        }
        weekDropdown.setValue(huidigeWeek);

        weekDropdown.setOnAction(e -> {
            huidigeWeek = weekDropdown.getValue();
            showAgendaView(); // herlaad agenda voor gekozen week
        });

        weekSelector.getChildren().addAll(weekLabel, weekDropdown);

        GridPane agendaGrid = new GridPane();
        agendaGrid.setHgap(10);
        agendaGrid.setVgap(10);
        agendaGrid.setPadding(new Insets(10));

        String[] dagen = {"Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag", "Zondag"};

        try {
            AgendaDAO dao = new AgendaDAO();

            for (int col = 0; col < dagen.length; col++) {
                String dag = dagen[col];
                Label dagLabel = new Label(dag);
                dagLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
                dagLabel.setTextFill(Color.web("#333"));
                agendaGrid.add(dagLabel, col, 0);

                List<String> opgeslagenItems = dao.getAgendaItems(currentUserId, huidigeWeek, dag);

                for (int row = 1; row <= 6; row++) {
                    StackPane vak = new StackPane();
                    vak.setPrefSize(140, 60);
                    vak.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 5;");

                    String tekst = (row <= opgeslagenItems.size()) ? opgeslagenItems.get(row - 1) : "";
                    Label inhoud = new Label(tekst);
                    inhoud.setWrapText(true);
                    inhoud.setFont(Font.font("Arial", 13));
                    inhoud.setTextFill(Color.web("#333"));

                    vak.getChildren().add(inhoud);

                    vak.setOnMouseClicked(ev -> {
                        TextInputDialog tijdDialog = new TextInputDialog();
                        tijdDialog.setTitle("Tijd invoeren");
                        tijdDialog.setHeaderText(null);
                        tijdDialog.setContentText("Vul de tijd in:");

                        Optional<String> tijdResult = tijdDialog.showAndWait();
                        tijdResult.ifPresent(tijd -> {
                            TextInputDialog activiteitDialog = new TextInputDialog();
                            activiteitDialog.setTitle("Activiteit invoeren");
                            activiteitDialog.setHeaderText(null);
                            activiteitDialog.setContentText("Wat wil je dan gaan doen?");

                            Optional<String> activiteitResult = activiteitDialog.showAndWait();
                            activiteitResult.ifPresent(activiteit -> {
                                String nieuweTekst = tijd + " - " + activiteit;
                                inhoud.setText(nieuweTekst);
                                try {
                                    dao.saveAgendaItem(currentUserId, huidigeWeek, dag, tijd, activiteit);
                                } catch (SQLException ex) {
                                    ex.printStackTrace();
                                }
                            });
                        });
                    });

                    agendaGrid.add(vak, col, row);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        agendaWrapper.getChildren().addAll(weekSelector, agendaGrid);
        mainLayout.setCenter(agendaWrapper);
    }
    // ==================== 3. UPLOAD VIEW ====================
    private void showUploadView() {
        BorderPane uploadLayout = new BorderPane();

        Label header = new Label("EduClock - Bewijs uploaden");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        header.setPadding(new Insets(20));
        uploadLayout.setTop(header);

        HBox content = new HBox(20);
        content.setPadding(new Insets(20));

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        form.setPrefWidth(600);

        Label lessonInfo = new Label("Vandaag • Ma 14:30-15:00 • Vak: Wiskunde • Onderwerp: Hoofdstuk 4");
        lessonInfo.setTextFill(Color.GRAY);

        Label titleLabel = new Label("Titel *");
        titleLabel.setTextFill(Color.RED);
        TextField titleField = new TextField("Samenvatting paragraaf 4.1");
        titleField.setStyle("-fx-font-size: 14; -fx-padding: 10;");

        Label notesLabel = new Label("Notities (optioneel)");
        TextArea notesArea = new TextArea("Bijv. wat ging goed? Wat moet ik herhalen?");
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);

        Label fileLabel = new Label("Bestand uploaden (foto/pdf) *");
        fileLabel.setTextFill(Color.RED);

        VBox uploadBox = new VBox(10);
        uploadBox.setPadding(new Insets(20));
        uploadBox.setStyle("-fx-background-color: #f5f7f8; -fx-border-color: #ddd; -fx-border-radius: 5;");
        uploadBox.setAlignment(Pos.CENTER);

        Label uploadHint = new Label("Sleep hier je bestand of klik om te kiezen");
        uploadHint.setTextFill(Color.GRAY);

        Label uploadedFileLabel = new Label();
        uploadedFileLabel.setStyle("-fx-background-color: #c8ffc8; -fx-padding: 8; -fx-background-radius: 4;");
        uploadedFileLabel.setVisible(false);

        Button chooseBtn = new Button("Kies bestand");
        chooseBtn.setStyle("-fx-font-size: 13; -fx-padding: 8 15;");
        chooseBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File file = fc.showOpenDialog(primaryStage);
            if (file != null) {
                uploadedFileLabel.setText(file.getName() + " • " + (file.length()/1024) + " KB");
                uploadedFileLabel.setVisible(true);
            }
        });

        uploadBox.getChildren().addAll(uploadHint, uploadedFileLabel, chooseBtn);
        form.getChildren().addAll(lessonInfo, titleLabel, titleField, notesLabel, notesArea, fileLabel, uploadBox);

        VBox rightPanel = new VBox(15);
        rightPanel.setPrefWidth(280);

        VBox details = new VBox(8);
        details.setPadding(new Insets(15));
        details.setStyle("-fx-background-color: #e6f0f5; -fx-background-radius: 5;");
        Label detailTitle = new Label("Blokdetails");
        detailTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        details.getChildren().addAll(detailTitle,
                new Label("Datum/tijd: Ma 14:30-15:00"),
                new Label("Vak: Wiskunde"),
                new Label("Onderwerp: H4 - Vergelijkingen"),
                new Label("Status: Blok voltooid") {{ setTextFill(Color.GREEN); }}
        );

        VBox tip = new VBox(8);
        tip.setPadding(new Insets(15));
        tip.setStyle("-fx-background-color: #fff4e6; -fx-background-radius: 5;");
        Label tipTitle = new Label("Tip");
        tipTitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        tip.getChildren().addAll(tipTitle,
                new Label("• Maak 1-2 duidelijke foto's"),
                new Label("• Schrijf 1 korte zin"),
                new Label("• Controleer je naam/klas")
        );

        rightPanel.getChildren().addAll(details, tip);
        content.getChildren().addAll(form, rightPanel);
        uploadLayout.setCenter(content);

        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(15));
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Annuleren");
        cancelBtn.setOnAction(e -> {
            titleField.clear();
            notesArea.clear();
        });

        Button uploadBtn = new Button("Uploaden");
        uploadBtn.setStyle("-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");
        uploadBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Bestand is geüpload!");
            alert.showAndWait();
        });

        buttons.getChildren().addAll(cancelBtn, uploadBtn);
        uploadLayout.setBottom(buttons);

        mainLayout.setCenter(uploadLayout);
        applyTheme();
    }

    // ==================== 4. BEOORDELING VIEW ====================
    private void showBeoordelingView() {
        BorderPane beoordelingLayout = new BorderPane();

        HBox topBar = new HBox();
        topBar.setPadding(new Insets(20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);

        Label title = new Label("Beoordeling");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 34));
        HBox.setHgrow(title, Priority.ALWAYS);

        ComboBox<String> filterCombo = new ComboBox<>();
        filterCombo.getItems().addAll("-select-", "GEMAAKT", "GEMAAKT EN BEOORDEELD", "NOG NIET GEMAAKT");
        filterCombo.setValue("-select-");

        topBar.getChildren().addAll(title, new Label("Filter:"), filterCombo);
        beoordelingLayout.setTop(topBar);

        VBox studentsList = new VBox(10);
        studentsList.setPadding(new Insets(20));

        addStudentRow(studentsList, "STEFAN ASLAN", "GEMAAKT");
        addStudentRow(studentsList, "JAZZ ROTH", "GEMAAKT");
        addStudentRow(studentsList, "MARVIALE FIRMA", "NOG NIET GEMAAKT");
        addStudentRow(studentsList, "JEREMIAH FREDERIK", "GEMAAKT EN BEOORDEELD");
        addStudentRow(studentsList, "PUK ANTON", "NOG NIET GEMAAKT");
        addStudentRow(studentsList, "YUSUF YILMAZ", "GEMAAKT EN BEOORDEELD");

        ScrollPane scroll = new ScrollPane(studentsList);
        scroll.setFitToWidth(true);
        beoordelingLayout.setCenter(scroll);

        mainLayout.setCenter(beoordelingLayout);
        applyTheme();
    }

    private void addStudentRow(VBox container, String name, String status) {
        VBox row = new VBox(5);

        HBox mainRow = new HBox(15);
        mainRow.setPadding(new Insets(12));
        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5; -fx-cursor: hand;");

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label statusLabel = new Label(" " + status + " ");
        statusLabel.setStyle(getStatusStyle(status));

        Button toggleBtn = new Button("▼");
        toggleBtn.setStyle("-fx-font-weight: bold; -fx-background-color: transparent;");

        mainRow.getChildren().addAll(nameLabel, statusLabel, toggleBtn);

        VBox details = new VBox(8);
        details.setPadding(new Insets(15, 15, 15, 40));
        details.setStyle("-fx-background-color: #f0f8ff;");
        details.setVisible(false);

        Hyperlink hwLink = new Hyperlink("huiswerk.doc");
        Hyperlink bewijsLink = new Hyperlink("bewijs.pdf");

        HBox feedbackBox = new HBox(8);
        TextField feedbackField = new TextField();
        feedbackField.setPromptText("Geef feedback...");
        feedbackField.setPrefWidth(300);
        Button sendBtn = new Button("Verstuur");
        sendBtn.setStyle("-fx-background-color: #008cd7; -fx-text-fill: white;");
        sendBtn.setOnAction(e -> {
            if (!feedbackField.getText().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Feedback verstuurd naar " + name);
                alert.showAndWait();
                feedbackField.clear();
            }
        });
        feedbackBox.getChildren().addAll(feedbackField, sendBtn);

        details.getChildren().addAll(hwLink, bewijsLink, feedbackBox);

        toggleBtn.setOnAction(e -> {
            details.setVisible(!details.isVisible());
            toggleBtn.setText(details.isVisible() ? "▲" : "▼");
        });

        row.getChildren().addAll(mainRow, details);
        container.getChildren().add(row);
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "GEMAAKT": return "-fx-background-color: #cce5ff; -fx-text-fill: #005aff; -fx-padding: 4 10; -fx-background-radius: 4;";
            case "GEMAAKT EN BEOORDEELD": return "-fx-background-color: #ccffcc; -fx-text-fill: #008200; -fx-padding: 4 10; -fx-background-radius: 4;";
            case "NOG NIET GEMAAKT": return "-fx-background-color: #ffcccc; -fx-text-fill: #c80000; -fx-padding: 4 10; -fx-background-radius: 4;";
            default: return "-fx-background-color: #e0e0e0; -fx-padding: 4 10; -fx-background-radius: 4;";
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
