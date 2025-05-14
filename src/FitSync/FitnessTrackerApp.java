package FitSync;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.Optional;

public class FitnessTrackerApp extends Application {

  private final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=FitSync_Database;encrypt=true;trustServerCertificate=true";
  private final String DB_USER = "LMS_Admin";
  private final String DB_PASSWORD = "SSMSpass07";

  @Override
  public void start(Stage primaryStage) {
    // ===== Title & Subtitle =====
    Label title = new Label("Create Your Account");
    title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

    Label subtitle = new Label("Start your fitness journey today");
    subtitle.setStyle("-fx-text-fill: grey; -fx-font-size: 12px;");

    // ===== Form Fields =====
    Label usernameLabel = new Label("Username");
    TextField usernameField = new TextField();
    usernameField.setPromptText("Enter your username");

    Label emailLabel = new Label("Email Address");
    TextField emailField = new TextField();
    emailField.setPromptText("Enter your email");

    Label passwordLabel = new Label("Password");
    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Create a password");

    Label confirmPasswordLabel = new Label("Confirm Password");
    PasswordField confirmPasswordField = new PasswordField();
    confirmPasswordField.setPromptText("Confirm your password");

    // ===== Submit Button =====
    Button createButton = new Button("Create Account");
    createButton.setStyle("-fx-background-color: #00A8E8; -fx-text-fill: white; -fx-font-weight: bold;");

    Label messageLabel = new Label();

    createButton.setOnAction(e -> {
      String username = usernameField.getText().trim();
      String email = emailField.getText().trim();
      String password = passwordField.getText();
      String confirmPassword = confirmPasswordField.getText();

      if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        messageLabel.setText("Please fill in all fields.");
        return;
      }

      if (!password.equals(confirmPassword)) {
        messageLabel.setText("Passwords do not match.");
        return;
      }

      int userId = registerUser(username, email, password);
      if (userId > 0) {
        setGoals((Stage) createButton.getScene().getWindow(), userId);
      } else {
        messageLabel.setText("Failed to create account. Username or email may already exist.");
      }

    });

    // ===== Form Container =====
    VBox formBox = new VBox(10,
        title,
        subtitle,
        usernameLabel, usernameField,
        emailLabel, emailField,
        passwordLabel, passwordField,
        confirmPasswordLabel, confirmPasswordField,
        createButton,
        messageLabel);

    formBox.setPadding(new Insets(30));
    formBox.setMaxWidth(300);
    formBox.setAlignment(Pos.TOP_LEFT);
    formBox.setStyle(
        "-fx-background-color: white; -fx-border-color: lightgray; -fx-border-width: 1px; -fx-border-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

    // ===== Right-aligned Container =====
    HBox mainContainer = new HBox();
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    mainContainer.getChildren().addAll(spacer, formBox);
    mainContainer.setPadding(new Insets(50));

    Scene scene = new Scene(mainContainer, 800, 500);
    primaryStage.setTitle("FitSync");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private int registerUser(String username, String email, String password) {
    String sql = "INSERT INTO [dbo].[User](username, email, password) VALUES (?, ?, ?)";
    String[] returnCols = { "userID" };

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement pstmt = conn.prepareStatement(sql, returnCols)) {

      pstmt.setString(1, username);
      pstmt.setString(2, email);
      pstmt.setString(3, password);
      int affectedRows = pstmt.executeUpdate();

      if (affectedRows > 0) {
        try (ResultSet rs = pstmt.getGeneratedKeys()) {
          if (rs.next()) {
            return rs.getInt(1);
          }
        }
      }
    } catch (SQLException ex) {
      System.out.println("Database error: " + ex.getMessage());
    }
    return -1;
  }

  private void setGoals(Stage primaryStage, int userId) {
    Label heading = new Label("Set Your Fitness Goals");
    heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    TextField startingWeightField = new TextField();
    startingWeightField.setPromptText("e.g., 43");

    TextField goalWeightField = new TextField();
    goalWeightField.setPromptText("e.g., 55");

    TextField weeklyGoalField = new TextField();
    weeklyGoalField.setPromptText("e.g., 0.5");

    ComboBox<String> activityLevelCombo = new ComboBox<>();
    activityLevelCombo.getItems().addAll("Not Very Active", "Lightly Active", "Active", "Very Active");
    activityLevelCombo.setValue("Lightly Active");

    TextField heightField = new TextField();
    heightField.setPromptText("Height in cm");

    ComboBox<String> genderCombo = new ComboBox<>();
    genderCombo.getItems().addAll("Male", "Female", "Other");
    genderCombo.setValue("Male");

    TextField ageField = new TextField();
    ageField.setPromptText("Age");

    Button saveButton = new Button("Save Goals");
    Label messageLabel = new Label();

    saveButton.setOnAction(e -> {
      try {
        float startingWeight = Float.parseFloat(startingWeightField.getText());
        float currentWeight = Float.parseFloat(startingWeightField.getText());
        float goalWeight = Float.parseFloat(goalWeightField.getText());
        float height = Float.parseFloat(heightField.getText());
        float weeklyGoal = Float.parseFloat(weeklyGoalField.getText());
        String activityLevel = activityLevelCombo.getValue();
        String gender = genderCombo.getValue();
        int age = Integer.parseInt(ageField.getText());

        String fitnessGoal = goalWeight > startingWeight ? "Gain" + weeklyGoal + "kg per week"
            : "Lose" + weeklyGoal + "kg per week";

        String sql = "INSERT INTO [dbo].[Profile](userID, height, currentWeight, targetWeight, fitnessGoal, activityLevel, gender, age, startingWeight) "
            +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

          pstmt.setInt(1, userId);
          pstmt.setFloat(2, height);
          pstmt.setFloat(3, currentWeight);
          pstmt.setFloat(4, goalWeight);
          pstmt.setString(5, fitnessGoal);
          pstmt.setString(6, activityLevel);
          pstmt.setString(7, gender);
          pstmt.setInt(8, age);
          pstmt.setFloat(9, startingWeight);

          pstmt.executeUpdate();
          showProfileAndGoals(primaryStage, userId);
        }

      } catch (Exception ex) {
        messageLabel.setText("Please enter valid input.");
        System.out.println("Error: " + ex.getMessage());
      }
    });

    VBox form = new VBox(10,
        heading,
        new Label("Starting Weight:"), startingWeightField,
        new Label("Goal Weight:"), goalWeightField,
        new Label("Weekly Goal (kg/week):"), weeklyGoalField,
        new Label("Activity Level:"), activityLevelCombo,
        new Label("Height (cm):"), heightField,
        new Label("Gender:"), genderCombo,
        new Label("Age:"), ageField,
        saveButton,
        messageLabel);

    form.setPadding(new Insets(30));
    form.setAlignment(Pos.CENTER_LEFT);
    ScrollPane scrollPane = new ScrollPane(form);
    scrollPane.setFitToWidth(true);

    Scene goalScene = new Scene(scrollPane, 500, 600);
    primaryStage.setScene(goalScene);
  }

  private void showProfileAndGoals(Stage primaryStage, int userId) {
    Label profileTitle = new Label("Profile");
    profileTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

    Label nameLabel = new Label();
    Label memberSinceLabel = new Label();
    Label ageLabel = new Label();
    Label heightLabel = new Label();

    String userSql = "SELECT u.username, u.registrationDate, p.age, p.height FROM [dbo].[User] u JOIN [dbo].[Profile] p ON u.userID = p.userID WHERE u.userID = ?";

    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement pstmt = conn.prepareStatement(userSql)) {
      pstmt.setInt(1, userId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        nameLabel.setText(rs.getString("username"));
        memberSinceLabel.setText("Member since " +
            rs.getDate("registrationDate").toLocalDate().getMonth() + " " +
            rs.getDate("registrationDate").toLocalDate().getYear());
        ageLabel.setText("Age: " + rs.getInt("age"));
        heightLabel.setText("Height: " + rs.getFloat("height") + " cm");
      }
    } catch (SQLException e) {
      System.out.println("Profile load error: " + e.getMessage());
    }

    Button goToUpdateProfile = new Button("Update Profile");
    goToUpdateProfile.setStyle("-fx-background-color: #007ACC; -fx-text-fill: white;");
    goToUpdateProfile.setOnAction(e -> openProfileUpdateWindow(userId, primaryStage));

    VBox profileBox = new VBox(10,
        nameLabel,
        memberSinceLabel,
        ageLabel,
        heightLabel,
        goToUpdateProfile);
    profileBox.setPadding(new Insets(10));
    profileBox.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: lightgray; -fx-border-radius: 5;");

    // ==== Goals View Only ====
    Label goalsTitle = new Label("Fitness Goals");
    goalsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    Label startingWeightLabel = new Label();
    Label currentWeightLabel = new Label();
    Label goalWeightLabel = new Label();
    Label weeklyGoalLabel = new Label();
    Label activityLevelLabel = new Label();

    String goalSql = "SELECT startingWeight, currentWeight, targetWeight, fitnessGoal, activityLevel FROM [dbo].[Profile] WHERE userID = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement pstmt = conn.prepareStatement(goalSql)) {
      pstmt.setInt(1, userId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        startingWeightLabel.setText("Starting Weight: " + rs.getFloat("startingWeight") + " kg");
        currentWeightLabel.setText("Current Weight: " + rs.getFloat("currentWeight") + " kg");
        goalWeightLabel.setText("Goal Weight: " + rs.getFloat("targetWeight") + " kg");
        weeklyGoalLabel.setText("Weekly Goal: " + rs.getString("fitnessGoal"));
        activityLevelLabel.setText("Activity Level: " + rs.getString("activityLevel"));
      }
    } catch (SQLException e) {
      System.out.println("Goals load error: " + e.getMessage());
    }

    Button goToUpdateGoals = new Button("Update Goals");
    goToUpdateGoals.setStyle("-fx-background-color: #00A8E8; -fx-text-fill: white;");
    goToUpdateGoals.setOnAction(e -> openGoalsUpdateWindow(userId, primaryStage));

    VBox goalsBox = new VBox(10,
        goalsTitle,
        startingWeightLabel,
        currentWeightLabel,
        goalWeightLabel,
        weeklyGoalLabel,
        activityLevelLabel,
        goToUpdateGoals);
    goalsBox.setPadding(new Insets(10));
    goalsBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: lightgray; -fx-border-radius: 5;");

    Button deleteAccountBtn = new Button("Delete Account");
    deleteAccountBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
    deleteAccountBtn.setAlignment(Pos.CENTER);
    deleteAccountBtn.setPadding(new Insets(10));

    deleteAccountBtn.setOnAction(e -> {
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle("Delete Account");
      confirm.setHeaderText("Are you sure you want to delete your account?");
      confirm.setContentText("This action cannot be undone.");

      Optional<ButtonType> result = confirm.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
          // Delete from Profile first
          String deleteProfile = "DELETE FROM [dbo].[Profile] WHERE userID = ?";
          try (PreparedStatement ps1 = conn.prepareStatement(deleteProfile)) {
            ps1.setInt(1, userId);
            ps1.executeUpdate();
          }

          // Then delete from User table
          String deleteUser = "DELETE FROM [dbo].[User] WHERE userID = ?";
          try (PreparedStatement ps2 = conn.prepareStatement(deleteUser)) {
            ps2.setInt(1, userId);
            ps2.executeUpdate();
          }

          Alert deleted = new Alert(Alert.AlertType.INFORMATION);
          deleted.setTitle("Account Deleted");
          deleted.setHeaderText(null);
          deleted.setContentText("Your account has been deleted.");
          deleted.showAndWait();

          // Redirect to login window
          start(primaryStage);

        } catch (SQLException ex) {
          System.out.println("Delete error: " + ex.getMessage());
        }
      }
    });

    VBox fullPage = new VBox(20, profileTitle, profileBox, goalsBox, deleteAccountBtn);
    fullPage.setPadding(new Insets(30));

    ScrollPane scrollPane = new ScrollPane(fullPage);
    scrollPane.setFitToWidth(true);

    BorderPane root = new BorderPane();
    root.setTop(createTopNav(userId, primaryStage));
    root.setCenter(scrollPane);

    Scene scene = new Scene(root, 600, 600);
    primaryStage.setScene(scene);
  }

  private void openProfileUpdateWindow(int userId, Stage parentStage) {
    Stage updateStage = new Stage();
    updateStage.setTitle("Update Profile");

    TextField ageField = new TextField();
    TextField heightField = new TextField();
    Label messageLabel = new Label();

    // Pre-fill current data
    String sql = "SELECT age, height FROM [dbo].[Profile] WHERE userID = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        ageField.setText(String.valueOf(rs.getInt("age")));
        heightField.setText(String.valueOf(rs.getFloat("height")));
      }
    } catch (SQLException e) {
      System.out.println("Load profile for update error: " + e.getMessage());
    }

    Button saveBtn = new Button("Save");
    saveBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
    saveBtn.setOnAction(e -> {
      try {
        int age = Integer.parseInt(ageField.getText());
        float height = Float.parseFloat(heightField.getText());

        String updateSql = "UPDATE [dbo].[Profile] SET age = ?, height = ? WHERE userID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
          pstmt.setInt(1, age);
          pstmt.setFloat(2, height);
          pstmt.setInt(3, userId);
          pstmt.executeUpdate();

          updateStage.close();
          showProfileAndGoals(parentStage, userId);
        }
      } catch (Exception ex) {
        messageLabel.setText("Please enter valid age and height.");
      }
    });

    VBox layout = new VBox(10,
        new Label("Age:"), ageField,
        new Label("Height (cm):"), heightField,
        saveBtn,
        messageLabel);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.CENTER);

    updateStage.setScene(new Scene(layout, 300, 250));
    updateStage.initOwner(parentStage);
    updateStage.show();
  }

  private void openGoalsUpdateWindow(int userId, Stage parentStage) {
    Stage updateStage = new Stage();
    updateStage.setTitle("Update Fitness Goals");

    TextField startField = new TextField();
    TextField currentField = new TextField();
    TextField targetField = new TextField();
    TextField weeklyField = new TextField();
    ComboBox<String> activityCombo = new ComboBox<>();
    activityCombo.getItems().addAll("Not Very Active", "Lightly Active", "Active", "Very Active");

    Label messageLabel = new Label();

    // Load current data
    String sql = "SELECT startingWeight, currentWeight, targetWeight, fitnessGoal, activityLevel FROM [dbo].[Profile] WHERE userID = ?";
    try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, userId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        startField.setText(String.valueOf(rs.getFloat("startingWeight")));
        currentField.setText(String.valueOf(rs.getFloat("currentWeight")));
        targetField.setText(String.valueOf(rs.getFloat("targetWeight")));
        weeklyField.setText(rs.getString("fitnessGoal").replaceAll("[^\\d.]", ""));
        activityCombo.setValue(rs.getString("activityLevel"));
      }
    } catch (SQLException e) {
      System.out.println("Load goals for update error: " + e.getMessage());
    }

    Button saveBtn = new Button("Save");
    saveBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
    saveBtn.setOnAction(e -> {
      try {
        float start = Float.parseFloat(startField.getText());
        float current = Float.parseFloat(currentField.getText());
        float goal = Float.parseFloat(targetField.getText());
        float weekly = Float.parseFloat(weeklyField.getText());
        String level = activityCombo.getValue();
        String fitnessGoal = goal > start ? "Gain" + weekly + "kg per week" : "Lose" + weekly + "kg per week";

        String updateSql = "UPDATE [dbo].[Profile] SET startingWeight = ?, currentWeight = ?, targetWeight = ?, fitnessGoal = ?, activityLevel = ? WHERE userID = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
          pstmt.setFloat(1, start);
          pstmt.setFloat(2, current);
          pstmt.setFloat(3, goal);
          pstmt.setString(4, fitnessGoal);
          pstmt.setString(5, level);
          pstmt.setInt(6, userId);
          pstmt.executeUpdate();

          updateStage.close();
          showProfileAndGoals(parentStage, userId);
        }
      } catch (Exception ex) {
        messageLabel.setText("Please check your inputs.");
      }
    });

    VBox layout = new VBox(10,
        new Label("Starting Weight:"), startField,
        new Label("Current Weight:"), currentField,
        new Label("Goal Weight:"), targetField,
        new Label("Weekly Goal:"), weeklyField,
        new Label("Activity Level:"), activityCombo,
        saveBtn,
        messageLabel);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.CENTER_LEFT);

    updateStage.setScene(new Scene(layout, 350, 450));
    updateStage.initOwner(parentStage);
    updateStage.show();
  }

  private HBox createTopNav(int userId, Stage primaryStage) {
    Label logo = new Label("🏋️‍♂️ FitSync");
    logo.setStyle("-fx-font-size: 18px; -fx-text-fill: #00A8E8; -fx-font-weight: bold;");

    Button dashboardBtn = new Button("Dashboard");
    Button profileBtn = new Button("Profile");
    Button workoutBtn = new Button("Workout");
    Button nutritionBtn = new Button("Nutrition");

    profileBtn.setOnAction(e -> showProfileAndGoals(primaryStage, userId));

    for (Button btn : new Button[] { dashboardBtn, profileBtn, workoutBtn, nutritionBtn }) {
      btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
    }

    HBox nav = new HBox(20, logo, dashboardBtn, profileBtn, workoutBtn, nutritionBtn);
    nav.setPadding(new Insets(10));
    nav.setAlignment(Pos.CENTER_LEFT);
    nav.setStyle("-fx-background-color: #4CB3D4;");

    return nav;
  }

  public static void main(String[] args) {
    launch(args);
  }
}