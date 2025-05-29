package com.trailtales.app;

import com.trailtales.config.AppConfig;
import com.trailtales.entity.User;
import com.trailtales.ui.LoginView;
import com.trailtales.ui.MainApplicationFrame;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp extends Application {

  private ApplicationContext springContext;
  private Stage primaryStage;

  @Override
  public void init() {
    springContext = new AnnotationConfigApplicationContext(AppConfig.class);
  }

  @Override
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    Application.setUserAgentStylesheet(
        new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());
    showLoginView();
  }

  public void showLoginView() {
    LoginView loginView = new LoginView(springContext, this::showMainApplicationFrame);
    primaryStage.setScene(loginView.createLoginScene(primaryStage));
    primaryStage.setTitle("TrailTales - Вхід");
    primaryStage.show();
  }

  public void showMainApplicationFrame(User loggedInUser) {
    MainApplicationFrame mainFrame =
        new MainApplicationFrame(
            springContext, loggedInUser, this::showLoginViewAfterLogout, this.primaryStage);
    primaryStage.setScene(mainFrame.createMainScene());
    primaryStage.setTitle("TrailTales - Головна");
    primaryStage.setMinWidth(1000);
    primaryStage.setMinHeight(700);
    primaryStage.setWidth(1200);
    primaryStage.setHeight(800);
    primaryStage.centerOnScreen();
  }

  public void showLoginViewAfterLogout() {
    showLoginView();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
