package com.trailtales.ui;

import com.trailtales.dto.UserLoginDto;
import com.trailtales.dto.UserRegistrationDto;
import com.trailtales.entity.User;
import com.trailtales.exception.AuthenticationException;
import com.trailtales.exception.UserAlreadyExistsException;
import com.trailtales.service.UserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

public class LoginView {

  private final UserService userService;
  private final Validator validator;
  private final Consumer<User> loginSuccessListener;

  // Стилі (можна винести в окремий клас або файл CSS)
  private static final String INPUT_STYLE =
      "-fx-background-color: #3c3f41; -fx-text-fill: #e0e0e0; -fx-border-color: #555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 7;";
  private static final String BUTTON_STYLE_PRIMARY =
      "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;";
  private static final String BUTTON_STYLE_SECONDARY =
      "-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";
  private static final String LABEL_STYLE = "-fx-text-fill: #c0c0c0;";

  public LoginView(ApplicationContext context, Consumer<User> loginSuccessListener) {
    this.userService = context.getBean(UserService.class);
    this.validator = context.getBean(Validator.class);
    this.loginSuccessListener = loginSuccessListener;
  }

  public Scene createLoginScene(Stage stage) {
    VBox layout = new VBox(20);
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(40));
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text sceneTitle = new Text("Ласкаво просимо до TrailTales");
    sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    sceneTitle.setFill(Color.WHITE);

    VBox formContainer = new VBox(15);
    formContainer.setMaxWidth(350);
    formContainer.setAlignment(Pos.CENTER);
    formContainer.setStyle(
        "-fx-background-color: #2b2b2b; -fx-padding: 30; -fx-background-radius: 10;");
    DropShadow shadow = new DropShadow();
    shadow.setColor(Color.rgb(0, 0, 0, 0.5));
    formContainer.setEffect(shadow);

    TextField usernameField = new TextField();
    usernameField.setPromptText("Ім'я користувача або Email");
    usernameField.setStyle(INPUT_STYLE);
    HBox usernameBox = new HBox(usernameField);
    HBox.setHgrow(usernameField, Priority.ALWAYS);

    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText("Пароль");
    passwordField.setStyle(INPUT_STYLE);
    HBox passwordBox = new HBox(passwordField);
    HBox.setHgrow(passwordField, Priority.ALWAYS);

    Button loginButton = new Button("Увійти");
    loginButton.setStyle(BUTTON_STYLE_PRIMARY);
    loginButton.setPrefWidth(Double.MAX_VALUE);

    Button registerLinkBtn = new Button("Немає акаунту? Зареєструватися");
    registerLinkBtn.setStyle(
        "-fx-background-color: transparent; -fx-text-fill: #0096C9; -fx-padding: 5 0;");

    Text messageText = new Text();
    messageText.setTextAlignment(TextAlignment.CENTER);
    messageText.setWrappingWidth(330);

    formContainer
        .getChildren()
        .addAll(usernameBox, passwordBox, loginButton, registerLinkBtn, messageText);
    layout.getChildren().addAll(sceneTitle, formContainer);

    loginButton.setOnAction(
        e -> {
          String identifier = usernameField.getText();
          String password = passwordField.getText();
          UserLoginDto loginDto = new UserLoginDto(identifier, password);
          Set<ConstraintViolation<UserLoginDto>> violations = validator.validate(loginDto);
          if (!violations.isEmpty()) {
            String errorMsg =
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n"));
            messageText.setText("Помилки валідації:\n" + errorMsg);
            messageText.setFill(Color.ORANGERED);
            return;
          }
          try {
            User currentUser = userService.loginUser(loginDto);
            messageText.setText("");
            loginSuccessListener.accept(currentUser);
          } catch (AuthenticationException ex) {
            messageText.setText("Помилка входу: " + ex.getMessage());
            messageText.setFill(Color.ORANGERED);
          } catch (IllegalArgumentException ex) {
            messageText.setText("Некоректні дані: " + ex.getMessage());
            messageText.setFill(Color.ORANGERED);
          } catch (Exception ex) {
            messageText.setText("Помилка входу: Невідома помилка.");
            messageText.setFill(Color.ORANGERED);
            ex.printStackTrace();
          }
        });

    registerLinkBtn.setOnAction(e -> stage.setScene(createRegisterScene(stage)));
    return new Scene(layout, 500, 600);
  }

  private Scene createRegisterScene(Stage stage) {
    VBox layout = new VBox(20);
    layout.setAlignment(Pos.CENTER);
    layout.setPadding(new Insets(40));
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text sceneTitle = new Text("TrailTales");
    sceneTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
    sceneTitle.setFill(Color.WHITE);

    Text subTitle = new Text("Створення вашого акаунту");
    subTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
    subTitle.setFill(Color.LIGHTGRAY);

    VBox formContainer = new VBox(15);
    formContainer.setMaxWidth(400);
    formContainer.setAlignment(Pos.CENTER_LEFT);
    formContainer.setStyle(
        "-fx-background-color: #2b2b2b; -fx-padding: 30; -fx-background-radius: 10;");
    DropShadow shadow = new DropShadow();
    shadow.setColor(Color.rgb(0, 0, 0, 0.5));
    formContainer.setEffect(shadow);

    Label usernameLabel = new Label("Ім'я користувача:");
    usernameLabel.setStyle(LABEL_STYLE);
    TextField regUsernameField = new TextField();
    regUsernameField.setPromptText("Ваше ім'я");
    regUsernameField.setStyle(INPUT_STYLE);

    Label emailLabel = new Label("Email:");
    emailLabel.setStyle(LABEL_STYLE);
    TextField regEmailField = new TextField();
    regEmailField.setPromptText("Email адреса");
    regEmailField.setStyle(INPUT_STYLE);

    Label passwordLabel = new Label("Пароль:");
    passwordLabel.setStyle(LABEL_STYLE);
    PasswordField regPasswordField = new PasswordField();
    regPasswordField.setPromptText("Пароль (мін. 6 символів)");
    regPasswordField.setStyle(INPUT_STYLE);

    Button registerButton = new Button("Зареєструватися");
    registerButton.setStyle(BUTTON_STYLE_PRIMARY);
    registerButton.setPrefWidth(Double.MAX_VALUE);

    Button backToLoginButton = new Button("Назад до Входу");
    backToLoginButton.setStyle(BUTTON_STYLE_SECONDARY);
    backToLoginButton.setPrefWidth(Double.MAX_VALUE);

    Text regMessageText = new Text();
    regMessageText.setTextAlignment(TextAlignment.CENTER);
    regMessageText.setWrappingWidth(380);

    formContainer
        .getChildren()
        .addAll(
            usernameLabel,
            regUsernameField,
            emailLabel,
            regEmailField,
            passwordLabel,
            regPasswordField,
            new Separator(javafx.geometry.Orientation.HORIZONTAL),
            registerButton,
            backToLoginButton,
            regMessageText);
    layout.getChildren().addAll(sceneTitle, subTitle, formContainer);

    registerButton.setOnAction(
        e -> {
          String username = regUsernameField.getText();
          String email = regEmailField.getText();
          String password = regPasswordField.getText();
          UserRegistrationDto registrationDto = new UserRegistrationDto(username, email, password);
          Set<ConstraintViolation<UserRegistrationDto>> violations =
              validator.validate(registrationDto);
          if (!violations.isEmpty()) {
            String errorMsg =
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n"));
            regMessageText.setText("Помилки валідації:\n" + errorMsg);
            regMessageText.setFill(Color.ORANGERED);
            return;
          }
          try {
            userService.registerUser(registrationDto);
            regMessageText.setText("Реєстрація успішна! Тепер ви можете увійти.");
            regMessageText.setFill(Color.LIGHTGREEN);
          } catch (UserAlreadyExistsException ex) {
            regMessageText.setText("Помилка реєстрації: " + ex.getMessage());
            regMessageText.setFill(Color.ORANGERED);
          } catch (IllegalArgumentException ex) {
            regMessageText.setText("Некоректні дані: " + ex.getMessage());
            regMessageText.setFill(Color.ORANGERED);
          } catch (Exception ex) {
            regMessageText.setText("Помилка реєстрації: Невідома помилка.");
            regMessageText.setFill(Color.ORANGERED);
            ex.printStackTrace();
          }
        });
    backToLoginButton.setOnAction(e -> stage.setScene(createLoginScene(stage)));
    return new Scene(layout, 550, 650);
  }

  private GridPane createGridPane() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.CENTER);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(25, 25, 25, 25));
    return grid;
  }
}
