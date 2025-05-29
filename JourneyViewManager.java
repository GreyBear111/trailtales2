package com.trailtales.ui.managers;

import com.trailtales.entity.Journey;
import com.trailtales.entity.RoleName;
import com.trailtales.entity.User;
import com.trailtales.service.JourneyService;
import com.trailtales.ui.MainApplicationFrame;
import com.trailtales.ui.UIConstants;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class JourneyViewManager {

  private final JourneyService journeyService;
  private final User currentUser;
  private final MainApplicationFrame mainAppFrame;
  private final AtomicBoolean showingAllJourneys = new AtomicBoolean(false);
  private final AtomicBoolean showingParticipatedJourneys =
      new AtomicBoolean(false); // Новий прапорець

  private ListView<Journey> journeyListView;
  private TextField searchField;

  public JourneyViewManager(
      JourneyService journeyService, User currentUser, MainApplicationFrame mainAppFrame) {
    this.journeyService = journeyService;
    this.currentUser = currentUser;
    this.mainAppFrame = mainAppFrame;
  }

  public Pane createJourneyPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle(UIConstants.BACKGROUND_STYLE_MAIN_APP);

    Text title = new Text("Список Подорожей");
    title.setStyle(UIConstants.TITLE_STYLE);
    layout.getChildren().add(title);

    searchField = new TextField();
    searchField.setPromptText("Пошук подорожей...");
    searchField.setStyle(UIConstants.INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    journeyListView = new ListView<>();
    journeyListView.setId("journeyListView");
    journeyListView.setStyle(UIConstants.LIST_VIEW_STYLE);
    // CellFactory буде встановлено в refreshJourneyList, щоб коректно відображати статус

    refreshJourneyList(searchField.getText()); // Перше завантаження списку
    searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshJourneyList(newVal));

    Button createBtn = new Button("Створити");
    createBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button viewAllBtn = new Button("Всі подорожі");
    viewAllBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button myJourneysBtn = new Button("Мої подорожі");
    myJourneysBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button participatedJourneysBtn = new Button("Де я учасник"); // Нова кнопка
    participatedJourneysBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);

    if (currentUser == null
        || !currentUser.getRoles().stream()
            .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
      viewAllBtn.setVisible(false);
      viewAllBtn.setManaged(false);
    }

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);
    journeyListView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              boolean isSelected = newSelection != null;
              // Дозволити редагування/видалення тільки власнику подорожі
              boolean canEditOrDelete =
                  isSelected
                      && currentUser != null
                      && newSelection.getUserId().equals(currentUser.getId());
              editBtn.setDisable(!canEditOrDelete);
              deleteBtn.setDisable(!canEditOrDelete);
            });

    createBtn.setOnAction(e -> mainAppFrame.showJourneyFormScene(null, this));
    editBtn.setOnAction(
        e -> {
          Journey selectedJourney = journeyListView.getSelectionModel().getSelectedItem();
          if (selectedJourney != null) {
            mainAppFrame.showJourneyFormScene(selectedJourney, this);
          }
        });
    deleteBtn.setOnAction(
        e -> {
          Journey selectedJourney = journeyListView.getSelectionModel().getSelectedItem();
          if (selectedJourney != null) {
            Alert confirmAlert =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Ви впевнені, що хочете видалити подорож '" + selectedJourney.getName() + "'?",
                    ButtonType.YES,
                    ButtonType.NO);
            confirmAlert.setTitle("Підтвердження видалення");
            confirmAlert.setHeaderText(null);
            mainAppFrame.applyDialogStyles(confirmAlert.getDialogPane());
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
              try {
                journeyService.deleteJourney(selectedJourney.getId(), currentUser);
                refreshJourneyList(searchField.getText());
                mainAppFrame.showAlert(Alert.AlertType.INFORMATION, "Успіх", "Подорож видалено.");
              } catch (SecurityException secEx) {
                mainAppFrame.showAlert(
                    Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
              } catch (Exception ex) {
                mainAppFrame.showAlert(
                    Alert.AlertType.ERROR, "Помилка видалення", "Не вдалося видалити подорож.");
                ex.printStackTrace();
              }
            }
          }
        });

    viewAllBtn.setOnAction(
        e -> {
          showingAllJourneys.set(true);
          showingParticipatedJourneys.set(false); // Скидаємо інший фільтр
          refreshJourneyList(searchField.getText());
        });
    myJourneysBtn.setOnAction(
        e -> {
          showingAllJourneys.set(false);
          showingParticipatedJourneys.set(false); // Скидаємо інший фільтр
          refreshJourneyList(searchField.getText());
        });
    participatedJourneysBtn.setOnAction(
        e -> { // Дія для нової кнопки
          showingAllJourneys.set(false); // Скидаємо інший фільтр
          showingParticipatedJourneys.set(true);
          refreshJourneyList(searchField.getText());
        });

    HBox filterButtons =
        new HBox(10, myJourneysBtn, viewAllBtn, participatedJourneysBtn); // Додаємо нову кнопку
    filterButtons.setAlignment(Pos.CENTER_LEFT);
    HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
    crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

    GridPane controlsGrid = new GridPane();
    controlsGrid.setHgap(10);
    controlsGrid.add(searchField, 0, 0);
    controlsGrid.add(filterButtons, 1, 0);
    controlsGrid.add(crudButtonsBox, 2, 0);
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.ALWAYS);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.SOMETIMES);
    ColumnConstraints col3 = new ColumnConstraints();
    col3.setHgrow(Priority.SOMETIMES);
    controlsGrid.getColumnConstraints().addAll(col1, col2, col3);

    layout.getChildren().addAll(controlsGrid, journeyListView);
    VBox.setVgrow(journeyListView, Priority.ALWAYS);
    return layout;
  }

  public void refreshJourneyList(String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase().trim() : "";
    ObservableList<Journey> sourceList;
    try {
      if (showingAllJourneys.get()) {
        sourceList = FXCollections.observableArrayList(journeyService.getAllJourneys());
      } else if (showingParticipatedJourneys.get()) {
        sourceList =
            FXCollections.observableArrayList(journeyService.getParticipatedJourneys(currentUser));
      } else {
        sourceList =
            FXCollections.observableArrayList(
                journeyService.getJourneysByUserId(currentUser.getId()));
      }
    } catch (Exception e) {
      mainAppFrame.showAlert(
          Alert.AlertType.ERROR,
          "Помилка",
          "Не вдалося оновити список подорожей: " + e.getMessage());
      sourceList = FXCollections.observableArrayList();
      e.printStackTrace();
    }

    ObservableList<Journey> filteredList;
    if (currentSearchText.isEmpty()) {
      filteredList = sourceList;
    } else {
      filteredList =
          sourceList.stream()
              .filter(
                  journey ->
                      (journey.getName() != null
                              && journey.getName().toLowerCase().contains(currentSearchText))
                          || (journey.getDescription() != null
                              && journey.getDescription().toLowerCase().contains(currentSearchText))
                          || (journey.getUser() != null
                              && journey
                                  .getUser()
                                  .getUsername()
                                  .toLowerCase()
                                  .contains(currentSearchText)) // Пошук за автором/власником
                  )
              .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    journeyListView.setItems(filteredList);

    journeyListView.setCellFactory(
        lv ->
            new ListCell<Journey>() {
              @Override
              protected void updateItem(Journey item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                } else {
                  String displayText = item.getName();
                  User journeyOwner = item.getUser(); // Власник подорожі, завантажений репозиторієм

                  if (journeyOwner != null) {
                    if (showingParticipatedJourneys.get()) {
                      // Якщо поточний користувач є власником у списку "Де я учасник" (що можливо,
                      // якщо він додав себе)
                      if (journeyOwner.getId().equals(currentUser.getId())) {
                        displayText += " (Ви власник)";
                      } else {
                        displayText += " (Власник: " + journeyOwner.getUsername() + ")";
                      }
                    } else if (showingAllJourneys.get()) {
                      displayText += " (Автор: " + journeyOwner.getUsername() + ")";
                    }
                    // Для "Мої подорожі" додаткова інформація про власника не потрібна, бо це
                    // завжди currentUser
                  }

                  setText(displayText);
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(UIConstants.LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });
  }

  public String getCurrentSearchText() {
    return searchField != null ? searchField.getText() : "";
  }
}
