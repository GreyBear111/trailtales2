package com.trailtales.ui;

import atlantafx.base.theme.PrimerDark;
import com.trailtales.dto.EventCreationDto;
import com.trailtales.dto.JourneyCreationDto;
import com.trailtales.dto.JourneyUpdateDto;
import com.trailtales.dto.LocationCreationDto;
import com.trailtales.dto.PhotoUploadDto;
import com.trailtales.dto.TagCreationDto;
import com.trailtales.entity.*;
import com.trailtales.service.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos; // Додано імпорт
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

public class MainApplicationFrame {

  private final UserService userService;
  private final EventService eventService;
  private final JourneyService journeyService;
  private final TagService tagService;
  private final LocationService locationService;
  private final PhotoService photoService;
  private final Validator validator;

  private User currentUser;
  private Stage primaryStage; // Зберігаємо primaryStage для модальних вікон
  private BorderPane mainLayout;
  private final AtomicBoolean showingAllJourneys = new AtomicBoolean(false);
  private final Runnable logoutCallback;

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  // Стилі UI елементів
  private static final String INPUT_STYLE =
      "-fx-background-color: #3c3f41; -fx-text-fill: #e0e0e0; -fx-border-color: #555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 7;";
  private static final String BUTTON_STYLE_PRIMARY =
      "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;";
  private static final String BUTTON_STYLE_SECONDARY =
      "-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";
  private static final String BUTTON_STYLE_DANGER =
      "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 6 12;";
  private static final String SIDEBAR_BUTTON_STYLE =
      "-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-alignment: baseline-left; -fx-padding: 10 15; -fx-border-color: transparent; -fx-border-width: 0 0 0 3px;";
  private static final String SIDEBAR_BUTTON_STYLE_ACTIVE =
      SIDEBAR_BUTTON_STYLE + "-fx-border-color: #007bff;";
  private static final String TITLE_STYLE =
      "-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: white;";
  private static final String LABEL_STYLE =
      "-fx-text-fill: #c0c0c0; -fx-min-width: 150px;"; // Додано -fx-min-width
  private static final String LIST_CELL_STYLE_NORMAL =
      "-fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-padding: 5px;";
  private static final String LIST_CELL_STYLE_SELECTED =
      "-fx-background-color: #005c99; -fx-text-fill: white; -fx-padding: 5px;";
  private static final String SCROLL_PANE_STYLE =
      "-fx-background-color: #2b2b2b; -fx-background: #2b2b2b;";

  public MainApplicationFrame(
      ApplicationContext context, User currentUser, Runnable logoutCallback) {
    this.userService = context.getBean(UserService.class);
    this.eventService = context.getBean(EventService.class);
    this.journeyService = context.getBean(JourneyService.class);
    this.tagService = context.getBean(TagService.class);
    this.locationService = context.getBean(LocationService.class);
    this.photoService = context.getBean(PhotoService.class);
    this.validator = context.getBean(Validator.class);
    this.currentUser = currentUser;
    this.logoutCallback = logoutCallback;
  }

  public Scene createMainScene(Stage stage) {
    this.primaryStage = stage;
    mainLayout = new BorderPane();
    mainLayout.setStyle("-fx-background-color: #1e1e1e;");

    VBox sideBar = createSideBar();
    mainLayout.setLeft(sideBar);

    Pane initialContent = createJourneyListPane();
    mainLayout.setCenter(initialContent);
    if (!sideBar.getChildren().isEmpty() && sideBar.getChildren().get(1) instanceof Button) {
      setActiveSidebarButton((Button) sideBar.getChildren().get(1));
    }
    return new Scene(mainLayout, 1200, 800);
  }

  private VBox createSideBar() {
    VBox sideBar = new VBox(0);
    sideBar.setPadding(new Insets(10, 0, 10, 0));
    sideBar.setStyle("-fx-background-color: #2b2b2b;");
    sideBar.setPrefWidth(220);

    Text appTitle = new Text("TrailTales");
    appTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    appTitle.setFill(Color.WHITE);
    HBox titleBox = new HBox(appTitle);
    titleBox.setAlignment(Pos.CENTER);
    titleBox.setPadding(new Insets(15, 0, 20, 0));
    sideBar.getChildren().add(titleBox);

    Button journeysBtn = createSidebarButton("Подорожі");
    Button eventsBtn = createSidebarButton("Події");
    Button tagsBtn = createSidebarButton("Теги");
    Button locationsBtn = createSidebarButton("Локації");
    Button photosBtn = createSidebarButton("Фотографії");
    Button logoutBtn = createSidebarButton("Вийти");

    journeysBtn.setOnAction(
        e -> {
          mainLayout.setCenter(createJourneyListPane());
          setActiveSidebarButton(journeysBtn);
        });
    eventsBtn.setOnAction(
        e -> {
          mainLayout.setCenter(createEventListPane());
          setActiveSidebarButton(eventsBtn);
        });
    tagsBtn.setOnAction(
        e -> {
          mainLayout.setCenter(createTagListPane());
          setActiveSidebarButton(tagsBtn);
        });
    locationsBtn.setOnAction(
        e -> {
          mainLayout.setCenter(createLocationListPane());
          setActiveSidebarButton(locationsBtn);
        });
    photosBtn.setOnAction(
        e -> {
          mainLayout.setCenter(createPhotoListPane());
          setActiveSidebarButton(photosBtn);
        });
    logoutBtn.setOnAction(
        e -> {
          currentUser = null;
          logoutCallback.run();
        });

    sideBar.getChildren().addAll(journeysBtn, eventsBtn, tagsBtn, locationsBtn, photosBtn);

    VBox bottomBox = new VBox(logoutBtn);
    VBox.setVgrow(bottomBox, Priority.ALWAYS);
    bottomBox.setAlignment(Pos.BOTTOM_LEFT);
    sideBar.getChildren().add(bottomBox);

    return sideBar;
  }

  private Button createSidebarButton(String text) {
    Button button = new Button(text);
    button.setStyle(SIDEBAR_BUTTON_STYLE);
    button.setPrefWidth(Double.MAX_VALUE);
    button.setOnMouseEntered(e -> button.setStyle(SIDEBAR_BUTTON_STYLE_ACTIVE));
    button.setOnMouseExited(
        e -> {
          if (!button.getProperties().containsKey("active")) {
            button.setStyle(SIDEBAR_BUTTON_STYLE);
          }
        });
    return button;
  }

  private void setActiveSidebarButton(Button activeButton) {
    if (mainLayout.getLeft() instanceof VBox) {
      VBox sideBar = (VBox) mainLayout.getLeft();
      sideBar
          .getChildren()
          .forEach(
              node -> {
                if (node instanceof Button) {
                  node.getProperties().remove("active");
                  node.setStyle(SIDEBAR_BUTTON_STYLE);
                }
              });
      activeButton.setStyle(SIDEBAR_BUTTON_STYLE_ACTIVE);
      activeButton.getProperties().put("active", true);
    }
  }

  // --- Панелі для розділів ---
  private Pane createJourneyListPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text title = new Text("Список Подорожей");
    title.setStyle(TITLE_STYLE);
    layout.getChildren().add(title);

    TextField searchField = new TextField();
    searchField.setPromptText("Пошук подорожей...");
    searchField.setStyle(INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    ListView<Journey> listView = new ListView<>();
    listView.setId("journeyListView");
    listView.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444;");
    listView.setCellFactory(
        lv ->
            new ListCell<Journey>() {
              @Override
              protected void updateItem(Journey item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                  setText(
                      item.getName()
                          + (showingAllJourneys.get() && item.getUser() != null
                              ? " (Автор: " + item.getUser().getUsername() + ")"
                              : ""));
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });

    refreshJourneyList(listView, searchField.getText());
    searchField
        .textProperty()
        .addListener((obs, oldVal, newVal) -> refreshJourneyList(listView, newVal));

    Button createBtn = new Button("Створити");
    createBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);
    Button viewAllBtn = new Button("Всі подорожі");
    viewAllBtn.setStyle(BUTTON_STYLE_SECONDARY);
    Button myJourneysBtn = new Button("Мої подорожі");
    myJourneysBtn.setStyle(BUTTON_STYLE_SECONDARY);

    if (currentUser == null
        || !currentUser.getRoles().stream()
            .anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
      viewAllBtn.setVisible(false);
      viewAllBtn.setManaged(false);
    }

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);
    listView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              boolean isSelected = newSelection != null;
              boolean canEditOrDelete =
                  isSelected
                      && currentUser != null
                      && newSelection.getUserId().equals(currentUser.getId());
              editBtn.setDisable(!canEditOrDelete);
              deleteBtn.setDisable(!canEditOrDelete);
            });

    createBtn.setOnAction(e -> showJourneyFormScene(null, listView));
    editBtn.setOnAction(
        e -> {
          Journey selectedJourney = listView.getSelectionModel().getSelectedItem();
          if (selectedJourney != null) {
            showJourneyFormScene(selectedJourney, listView);
          }
        });
    deleteBtn.setOnAction(
        e -> {
          Journey selectedJourney = listView.getSelectionModel().getSelectedItem();
          if (selectedJourney != null) {
            Alert confirmAlert =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Ви впевнені, що хочете видалити подорож '" + selectedJourney.getName() + "'?",
                    ButtonType.YES,
                    ButtonType.NO);
            confirmAlert.setTitle("Підтвердження видалення");
            confirmAlert.setHeaderText(null);
            applyDialogStyles(confirmAlert.getDialogPane());
            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
              try {
                journeyService.deleteJourney(selectedJourney.getId(), currentUser);
                refreshJourneyList(listView, searchField.getText());
                showAlert(Alert.AlertType.INFORMATION, "Успіх", "Подорож видалено.");
              } catch (SecurityException secEx) {
                showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
              } catch (Exception ex) {
                showAlert(
                    Alert.AlertType.ERROR, "Помилка видалення", "Не вдалося видалити подорож.");
                ex.printStackTrace();
              }
            }
          }
        });

    viewAllBtn.setOnAction(
        e -> {
          showingAllJourneys.set(true);
          refreshJourneyList(listView, searchField.getText());
        });
    myJourneysBtn.setOnAction(
        e -> {
          showingAllJourneys.set(false);
          refreshJourneyList(listView, searchField.getText());
        });

    HBox filterButtons = new HBox(10, myJourneysBtn, viewAllBtn);
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

    layout.getChildren().addAll(controlsGrid, listView);
    VBox.setVgrow(listView, Priority.ALWAYS);
    return layout;
  }

  private Pane createEventListPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text title = new Text("Список Подій");
    title.setStyle(TITLE_STYLE);
    layout.getChildren().add(title);

    TextField searchField = new TextField();
    searchField.setPromptText("Пошук подій...");
    searchField.setStyle(INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    ListView<Event> listView = new ListView<>();
    listView.setId("eventListView");
    listView.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444;");

    refreshEventList(listView, searchField.getText());
    searchField
        .textProperty()
        .addListener((obs, oldVal, newVal) -> refreshEventList(listView, newVal));

    listView.setCellFactory(
        lv ->
            new ListCell<Event>() {
              @Override
              protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                  String journeyName = "";
                  if (item.getJourneyId() != null) {
                    Optional<Journey> journeyOpt =
                        journeyService.getJourneyById(item.getJourneyId());
                    journeyName = journeyOpt.map(j -> " (Подорож: " + j.getName() + ")").orElse("");
                  }
                  setText(
                      item.getName()
                          + (item.getEventDate() != null
                              ? " [" + DATE_FORMATTER.format(item.getEventDate()) + "]"
                              : "")
                          + journeyName);
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });

    Button createBtn = new Button("Створити");
    createBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    listView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              boolean canModify = false;
              if (newVal != null && currentUser != null) {
                if (newVal.getJourneyId() != null) {
                  Optional<Journey> journeyOpt =
                      journeyService.getJourneyById(newVal.getJourneyId());
                  canModify =
                      journeyOpt.map(j -> j.getUserId().equals(currentUser.getId())).orElse(false);
                } else {
                  canModify = true;
                }
              }
              editBtn.setDisable(!canModify);
              deleteBtn.setDisable(!canModify);
            });

    createBtn.setOnAction(e -> showEventFormScene(null, listView));
    editBtn.setOnAction(
        e -> {
          Event selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) showEventFormScene(selected, listView);
        });
    deleteBtn.setOnAction(
        e -> {
          Event selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            Alert confirm =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Видалити подію '" + selected.getName() + "'?",
                    ButtonType.YES,
                    ButtonType.NO);
            applyDialogStyles(confirm.getDialogPane());
            confirm
                .showAndWait()
                .ifPresent(
                    response -> {
                      if (response == ButtonType.YES) {
                        try {
                          eventService.deleteEvent(selected.getId(), currentUser);
                          refreshEventList(listView, searchField.getText());
                          showAlert(Alert.AlertType.INFORMATION, "Успіх", "Подію видалено.");
                        } catch (SecurityException secEx) {
                          showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                        } catch (Exception ex) {
                          showAlert(
                              Alert.AlertType.ERROR,
                              "Помилка",
                              "Не вдалося видалити подію: " + ex.getMessage());
                        }
                      }
                    });
          }
        });

    HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
    crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

    GridPane controlsGrid = new GridPane();
    controlsGrid.setHgap(10);
    controlsGrid.add(searchField, 0, 0);
    controlsGrid.add(crudButtonsBox, 1, 0);
    ColumnConstraints colSearch = new ColumnConstraints();
    colSearch.setHgrow(Priority.ALWAYS);
    ColumnConstraints colButtons = new ColumnConstraints();
    colButtons.setHgrow(Priority.SOMETIMES);
    controlsGrid.getColumnConstraints().addAll(colSearch, colButtons);

    layout.getChildren().addAll(controlsGrid, listView);
    VBox.setVgrow(listView, Priority.ALWAYS);
    return layout;
  }

  private Pane createTagListPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text title = new Text("Список Тегів");
    title.setStyle(TITLE_STYLE);
    layout.getChildren().add(title);

    TextField searchField = new TextField();
    searchField.setPromptText("Пошук тегів...");
    searchField.setStyle(INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    ListView<Tag> listView = new ListView<>();
    listView.setId("tagListView");
    listView.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444;");

    refreshTagList(listView, searchField.getText());
    searchField
        .textProperty()
        .addListener((obs, oldVal, newVal) -> refreshTagList(listView, newVal));

    listView.setCellFactory(
        lv ->
            new ListCell<Tag>() {
              @Override
              protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                  setText(item.getName());
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });

    Button createBtn = new Button("Створити");
    createBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    listView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              boolean disabled = newVal == null;
              editBtn.setDisable(disabled);
              deleteBtn.setDisable(disabled);
            });

    createBtn.setOnAction(e -> showTagFormScene(null, listView));
    editBtn.setOnAction(
        e -> {
          Tag selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) showTagFormScene(selected, listView);
        });
    deleteBtn.setOnAction(
        e -> {
          Tag selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            Alert confirm =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Видалити тег '" + selected.getName() + "'? Це може вплинути на подорожі.",
                    ButtonType.YES,
                    ButtonType.NO);
            applyDialogStyles(confirm.getDialogPane());
            confirm
                .showAndWait()
                .ifPresent(
                    response -> {
                      if (response == ButtonType.YES) {
                        try {
                          tagService.deleteTag(selected.getId());
                          refreshTagList(listView, searchField.getText());
                          showAlert(Alert.AlertType.INFORMATION, "Успіх", "Тег видалено.");
                        } catch (Exception ex) {
                          showAlert(
                              Alert.AlertType.ERROR,
                              "Помилка",
                              "Не вдалося видалити тег: " + ex.getMessage());
                        }
                      }
                    });
          }
        });

    HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
    crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

    GridPane controlsGrid = new GridPane();
    controlsGrid.setHgap(10);
    controlsGrid.add(searchField, 0, 0);
    controlsGrid.add(crudButtonsBox, 1, 0);
    ColumnConstraints colSearch = new ColumnConstraints();
    colSearch.setHgrow(Priority.ALWAYS);
    ColumnConstraints colButtons = new ColumnConstraints();
    colButtons.setHgrow(Priority.SOMETIMES);
    controlsGrid.getColumnConstraints().addAll(colSearch, colButtons);

    layout.getChildren().addAll(controlsGrid, listView);
    VBox.setVgrow(listView, Priority.ALWAYS);
    return layout;
  }

  private Pane createLocationListPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text title = new Text("Список Локацій");
    title.setStyle(TITLE_STYLE);
    layout.getChildren().add(title);

    TextField searchField = new TextField();
    searchField.setPromptText("Пошук локацій...");
    searchField.setStyle(INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    ListView<Location> listView = new ListView<>();
    listView.setId("locationListView");
    listView.setStyle("-fx-background-color: #2b2b2b; -fx-border-color: #444;");

    refreshLocationList(listView, searchField.getText());
    searchField
        .textProperty()
        .addListener((obs, oldVal, newVal) -> refreshLocationList(listView, newVal));

    listView.setCellFactory(
        lv ->
            new ListCell<Location>() {
              @Override
              protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                  setText(
                      item.getName()
                          + (item.getDescription() != null && !item.getDescription().isEmpty()
                              ? " (" + item.getDescription() + ")"
                              : ""));
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });

    Button createBtn = new Button("Створити");
    createBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    listView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              boolean disabled = newVal == null;
              editBtn.setDisable(disabled);
              deleteBtn.setDisable(disabled);
            });

    createBtn.setOnAction(e -> showLocationFormScene(null, listView));
    editBtn.setOnAction(
        e -> {
          Location selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) showLocationFormScene(selected, listView);
        });
    deleteBtn.setOnAction(
        e -> {
          Location selected = listView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            Alert confirm =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Видалити локацію '"
                        + selected.getName()
                        + "'? Це може вплинути на подорожі та події.",
                    ButtonType.YES,
                    ButtonType.NO);
            applyDialogStyles(confirm.getDialogPane());
            confirm
                .showAndWait()
                .ifPresent(
                    response -> {
                      if (response == ButtonType.YES) {
                        try {
                          locationService.deleteLocation(selected.getId());
                          refreshLocationList(listView, searchField.getText());
                          showAlert(Alert.AlertType.INFORMATION, "Успіх", "Локацію видалено.");
                        } catch (Exception ex) {
                          showAlert(
                              Alert.AlertType.ERROR,
                              "Помилка",
                              "Не вдалося видалити локацію: " + ex.getMessage());
                        }
                      }
                    });
          }
        });

    HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
    crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

    GridPane controlsGrid = new GridPane();
    controlsGrid.setHgap(10);
    controlsGrid.add(searchField, 0, 0);
    controlsGrid.add(crudButtonsBox, 1, 0);
    ColumnConstraints colSearch = new ColumnConstraints();
    colSearch.setHgrow(Priority.ALWAYS);
    ColumnConstraints colButtons = new ColumnConstraints();
    colButtons.setHgrow(Priority.SOMETIMES);
    controlsGrid.getColumnConstraints().addAll(colSearch, colButtons);

    layout.getChildren().addAll(controlsGrid, listView);
    VBox.setVgrow(listView, Priority.ALWAYS);
    return layout;
  }

  private Pane createPhotoListPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle("-fx-background-color: #1e1e1e;");

    Text title = new Text("Галерея Фотографій");
    title.setStyle(TITLE_STYLE);

    TextField journeyIdField = new TextField();
    journeyIdField.setPromptText("Введіть ID подорожі для фото");
    journeyIdField.setStyle(INPUT_STYLE);

    Button loadPhotosBtn = new Button("Показати фото");
    loadPhotosBtn.setStyle(BUTTON_STYLE_SECONDARY);

    Label journeyIdLabel = new Label("ID Подорожі:");
    journeyIdLabel.setStyle(LABEL_STYLE);
    HBox journeyInputBox = new HBox(10, journeyIdLabel, journeyIdField, loadPhotosBtn);
    journeyInputBox.setAlignment(Pos.CENTER_LEFT);

    TilePane photoTilePane = new TilePane();
    photoTilePane.setPadding(new Insets(10));
    photoTilePane.setHgap(10);
    photoTilePane.setVgap(10);
    photoTilePane.setStyle("-fx-background-color: #2b2b2b;");

    ScrollPane scrollPane = new ScrollPane(photoTilePane);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true); // Додано для розтягування по висоті
    scrollPane.setStyle(SCROLL_PANE_STYLE);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    Button uploadBtn = new Button("Завантажити нове фото");
    uploadBtn.setStyle(BUTTON_STYLE_PRIMARY);
    uploadBtn.setDisable(true);

    journeyIdField
        .textProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              uploadBtn.setDisable(newVal == null || newVal.trim().isEmpty());
            });
    loadPhotosBtn.setOnAction(e -> refreshPhotoTilePane(journeyIdField, photoTilePane));
    uploadBtn.setOnAction(
        e -> {
          String journeyIdText = journeyIdField.getText();
          try {
            long journeyId = Long.parseLong(journeyIdText);
            Optional<Journey> journeyOpt = journeyService.getJourneyById(journeyId);
            if (journeyOpt.isEmpty()) {
              showAlert(
                  Alert.AlertType.ERROR, "Помилка", "Подорож з ID " + journeyId + " не знайдено.");
              return;
            }
            if (!journeyOpt.get().getUserId().equals(currentUser.getId())) {
              showAlert(
                  Alert.AlertType.ERROR,
                  "Помилка доступу",
                  "Ви можете завантажувати фото тільки до своїх подорожей.");
              return;
            }
            showPhotoUploadFormScene(
                journeyId, () -> refreshPhotoTilePane(journeyIdField, photoTilePane));
          } catch (NumberFormatException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка", "ID подорожі має бути числом.");
          }
        });

    layout.getChildren().addAll(title, journeyInputBox, scrollPane, uploadBtn);
    return layout;
  }

  // --- Форми для створення/редагування ---

  private void showJourneyFormScene(Journey journeyToEdit, ListView<Journey> journeyListView) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(journeyToEdit == null ? "Створення нової подорожі" : "Редагування подорожі");

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label nameLabel = new Label("Назва:");
    nameLabel.setStyle(LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField();
    nameField.setStyle(INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea = new TextArea();
    descriptionArea.setStyle(INPUT_STYLE);
    descriptionArea.setWrapText(true);
    grid.add(descriptionArea, 1, rowIndex++);

    Label startDateLabel = new Label("Дата початку (дд.мм.рррр):");
    startDateLabel.setStyle(LABEL_STYLE);
    grid.add(startDateLabel, 0, rowIndex);
    DatePicker startDatePicker = new DatePicker();
    startDatePicker.setStyle(INPUT_STYLE);
    grid.add(startDatePicker, 1, rowIndex++);

    Label endDateLabel = new Label("Дата кінця (дд.мм.рррр):");
    endDateLabel.setStyle(LABEL_STYLE);
    grid.add(endDateLabel, 0, rowIndex);
    DatePicker endDatePicker = new DatePicker();
    endDatePicker.setStyle(INPUT_STYLE);
    grid.add(endDatePicker, 1, rowIndex++);

    Label originLocLabel = new Label("Початкова локація:");
    originLocLabel.setStyle(LABEL_STYLE);
    grid.add(originLocLabel, 0, rowIndex);
    TextField originLocationField = new TextField();
    originLocationField.setStyle(INPUT_STYLE);
    grid.add(originLocationField, 1, rowIndex++);

    Label originLocDescLabel = new Label("Опис початкової локації:");
    originLocDescLabel.setStyle(LABEL_STYLE);
    grid.add(originLocDescLabel, 0, rowIndex);
    TextField originLocationDescField = new TextField();
    originLocationDescField.setStyle(INPUT_STYLE);
    grid.add(originLocationDescField, 1, rowIndex++);

    Label destLocLabel = new Label("Кінцева локація:");
    destLocLabel.setStyle(LABEL_STYLE);
    grid.add(destLocLabel, 0, rowIndex);
    TextField destLocationField = new TextField();
    destLocationField.setStyle(INPUT_STYLE);
    grid.add(destLocationField, 1, rowIndex++);

    Label destLocDescLabel = new Label("Опис кінцевої локації:");
    destLocDescLabel.setStyle(LABEL_STYLE);
    grid.add(destLocDescLabel, 0, rowIndex);
    TextField destLocationDescField = new TextField();
    destLocationDescField.setStyle(INPUT_STYLE);
    grid.add(destLocationDescField, 1, rowIndex++);

    Label tagsLabel = new Label("Теги (через кому):");
    tagsLabel.setStyle(LABEL_STYLE);
    grid.add(tagsLabel, 0, rowIndex);
    TextField tagsField = new TextField();
    tagsField.setStyle(INPUT_STYLE);
    grid.add(tagsField, 1, rowIndex++);

    if (journeyToEdit != null) {
      nameField.setText(journeyToEdit.getName());
      descriptionArea.setText(journeyToEdit.getDescription());
      startDatePicker.setValue(journeyToEdit.getStartDate());
      endDatePicker.setValue(journeyToEdit.getEndDate());
      if (journeyToEdit.getOriginLocation() != null) {
        originLocationField.setText(journeyToEdit.getOriginLocation().getName());
        originLocationDescField.setText(
            journeyToEdit.getOriginLocation().getDescription() != null
                ? journeyToEdit.getOriginLocation().getDescription()
                : "");
      }
      if (journeyToEdit.getDestinationLocation() != null) {
        destLocationField.setText(journeyToEdit.getDestinationLocation().getName());
        destLocationDescField.setText(
            journeyToEdit.getDestinationLocation().getDescription() != null
                ? journeyToEdit.getDestinationLocation().getDescription()
                : "");
      }
      if (journeyToEdit.getTags() != null && !journeyToEdit.getTags().isEmpty()) {
        tagsField.setText(
            journeyToEdit.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")));
      }
    }

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
    HBox buttons = new HBox(10, cancelBtn, saveBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(buttons, 1, rowIndex);

    saveBtn.setOnAction(
        e -> {
          try {
            Set<String> tagNames = new HashSet<>();
            if (!tagsField.getText().trim().isEmpty()) {
              tagNames.addAll(Arrays.asList(tagsField.getText().trim().split("\\s*,\\s*")));
            }

            if (journeyToEdit == null) {
              JourneyCreationDto dto =
                  new JourneyCreationDto(
                      nameField.getText(),
                      descriptionArea.getText(),
                      startDatePicker.getValue(),
                      endDatePicker.getValue(),
                      tagNames,
                      originLocationField.getText(),
                      originLocationDescField.getText(),
                      destLocationField.getText(),
                      destLocationDescField.getText());

              Set<ConstraintViolation<JourneyCreationDto>> violations = validator.validate(dto);
              if (!violations.isEmpty()) {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Помилка валідації",
                    violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n")));
                return;
              }

              journeyService.createJourney(dto, currentUser);
              showAlert(
                  Alert.AlertType.INFORMATION,
                  "Успіх",
                  "Подорож '" + dto.getName() + "' створено.");
            } else {
              JourneyUpdateDto dto =
                  new JourneyUpdateDto(
                      nameField.getText(),
                      descriptionArea.getText(),
                      startDatePicker.getValue(),
                      endDatePicker.getValue(),
                      tagNames,
                      originLocationField.getText(),
                      originLocationDescField.getText(),
                      destLocationField.getText(),
                      destLocationDescField.getText());

              Set<ConstraintViolation<JourneyUpdateDto>> violations = validator.validate(dto);
              if (!violations.isEmpty()) {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Помилка валідації",
                    violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n")));
                return;
              }

              journeyService.updateJourney(journeyToEdit.getId(), dto, currentUser);
              showAlert(
                  Alert.AlertType.INFORMATION,
                  "Успіх",
                  "Подорож '" + dto.getName() + "' оновлено.");
            }
            formStage.close();
            refreshJourneyList(journeyListView, "");
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
            ex.printStackTrace();
          }
        });

    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 600, 500);
    scene.setFill(Color.web("#2b2b2b")); // Встановлюємо фон сцени
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  private void showEventFormScene(Event eventToEdit, ListView<Event> eventListView) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(eventToEdit == null ? "Створення нової події" : "Редагування події");

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label nameLabel = new Label("Назва:");
    nameLabel.setStyle(LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField();
    nameField.setStyle(INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea = new TextArea();
    descriptionArea.setStyle(INPUT_STYLE);
    grid.add(descriptionArea, 1, rowIndex++);

    Label dateLabel = new Label("Дата (дд.мм.рррр):");
    dateLabel.setStyle(LABEL_STYLE);
    grid.add(dateLabel, 0, rowIndex);
    DatePicker datePicker = new DatePicker();
    datePicker.setStyle(INPUT_STYLE);
    grid.add(datePicker, 1, rowIndex++);

    Label timeLabel = new Label("Час (гг:хх):");
    timeLabel.setStyle(LABEL_STYLE);
    grid.add(timeLabel, 0, rowIndex);
    TextField timeField = new TextField();
    timeField.setStyle(INPUT_STYLE);
    grid.add(timeField, 1, rowIndex++);

    Label locNameLabel = new Label("Локація:");
    locNameLabel.setStyle(LABEL_STYLE);
    grid.add(locNameLabel, 0, rowIndex);
    TextField locationNameField = new TextField();
    locationNameField.setStyle(INPUT_STYLE);
    grid.add(locationNameField, 1, rowIndex++);

    Label locDescLabel = new Label("Опис локації:");
    locDescLabel.setStyle(LABEL_STYLE);
    grid.add(locDescLabel, 0, rowIndex);
    TextField locationDescField = new TextField();
    locationDescField.setStyle(INPUT_STYLE);
    grid.add(locationDescField, 1, rowIndex++);

    Label journeyIdLabelEv = new Label("ID Подорожі (необов'язково):");
    journeyIdLabelEv.setStyle(LABEL_STYLE);
    grid.add(journeyIdLabelEv, 0, rowIndex);
    TextField journeyIdField = new TextField();
    journeyIdField.setStyle(INPUT_STYLE);
    grid.add(journeyIdField, 1, rowIndex++);

    if (eventToEdit != null) {
      nameField.setText(eventToEdit.getName());
      descriptionArea.setText(eventToEdit.getDescription());
      datePicker.setValue(eventToEdit.getEventDate());
      if (eventToEdit.getEventTime() != null) {
        timeField.setText(eventToEdit.getEventTime().format(TIME_FORMATTER));
      }
      if (eventToEdit.getLocationId() != null) {
        locationService
            .getLocationById(eventToEdit.getLocationId())
            .ifPresent(
                loc -> {
                  locationNameField.setText(loc.getName());
                  locationDescField.setText(
                      loc.getDescription() != null ? loc.getDescription() : "");
                });
      }
      if (eventToEdit.getJourneyId() != null) {
        journeyIdField.setText(eventToEdit.getJourneyId().toString());
      }
    }

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
    HBox buttons = new HBox(10, cancelBtn, saveBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(buttons, 1, rowIndex);

    saveBtn.setOnAction(
        e -> {
          try {
            Long journeyId =
                journeyIdField.getText().trim().isEmpty()
                    ? null
                    : Long.parseLong(journeyIdField.getText().trim());
            LocalTime eventTime = parseTime(timeField.getText());

            if (eventToEdit == null) {
              EventCreationDto dto =
                  new EventCreationDto(
                      nameField.getText(),
                      descriptionArea.getText(),
                      datePicker.getValue(),
                      eventTime,
                      journeyId,
                      locationNameField.getText(),
                      locationDescField.getText());

              Set<ConstraintViolation<EventCreationDto>> violations = validator.validate(dto);
              if (!violations.isEmpty()) {
                showAlert(
                    Alert.AlertType.ERROR,
                    "Помилка валідації",
                    violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("\n")));
                return;
              }

              eventService.createEvent(dto, currentUser);
              showAlert(
                  Alert.AlertType.INFORMATION, "Успіх", "Подію '" + dto.getName() + "' створено.");
            } else {
              eventToEdit.setName(nameField.getText());
              eventToEdit.setDescription(descriptionArea.getText());
              eventToEdit.setEventDate(datePicker.getValue());
              eventToEdit.setEventTime(eventTime);
              eventToEdit.setJourneyId(journeyId);

              if (!locationNameField.getText().trim().isEmpty()) {
                Location loc =
                    locationService
                        .getLocationByName(locationNameField.getText().trim())
                        .orElseGet(
                            () ->
                                locationService.createLocation(
                                    locationNameField.getText().trim(),
                                    locationDescField.getText().trim()));
                eventToEdit.setLocationId(loc.getId());
              } else {
                eventToEdit.setLocationId(null);
              }

              eventService.updateEvent(eventToEdit, currentUser);
              showAlert(
                  Alert.AlertType.INFORMATION,
                  "Успіх",
                  "Подію '" + eventToEdit.getName() + "' оновлено.");
            }
            formStage.close();
            refreshEventList(eventListView, "");
          } catch (NumberFormatException nfe) {
            showAlert(Alert.AlertType.ERROR, "Помилка формату", "ID подорожі має бути числом.");
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
            ex.printStackTrace();
          }
        });

    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 500, 450);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  private void showLocationFormScene(Location locationToEdit, ListView<Location> locationListView) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(locationToEdit == null ? "Створення нової локації" : "Редагування локації");

    GridPane grid = createGridPane();
    int rowIndex = 0;
    Label nameLabel = new Label("Назва:");
    nameLabel.setStyle(LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField(locationToEdit != null ? locationToEdit.getName() : "");
    nameField.setStyle(INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea =
        new TextArea(
            locationToEdit != null && locationToEdit.getDescription() != null
                ? locationToEdit.getDescription()
                : "");
    descriptionArea.setStyle(INPUT_STYLE);
    descriptionArea.setWrapText(true);
    descriptionArea.setPrefRowCount(3);
    grid.add(descriptionArea, 1, rowIndex++);

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
    HBox buttons = new HBox(10, cancelBtn, saveBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(buttons, 1, rowIndex);

    saveBtn.setOnAction(
        e -> {
          String name = nameField.getText();
          String description = descriptionArea.getText();
          LocationCreationDto dto = new LocationCreationDto(name, description);
          Set<ConstraintViolation<LocationCreationDto>> violations = validator.validate(dto);
          if (!violations.isEmpty()) {
            showAlert(
                Alert.AlertType.ERROR,
                "Помилка валідації",
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n")));
            return;
          }
          try {
            if (locationToEdit == null) {
              Location newLocation =
                  locationService.createLocation(dto.getName(), dto.getDescription());
              showAlert(
                  Alert.AlertType.INFORMATION,
                  "Успіх",
                  "Локацію '" + newLocation.getName() + "' створено.");
            } else {
              Location updatedLocation =
                  locationService.updateLocation(
                      locationToEdit.getId(), dto.getName(), dto.getDescription());
              showAlert(
                  Alert.AlertType.INFORMATION,
                  "Успіх",
                  "Локацію '" + updatedLocation.getName() + "' оновлено.");
            }
            formStage.close();
            refreshLocationList(locationListView, "");
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
          }
        });
    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 450, 300);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  private void showPhotoUploadFormScene(long journeyId, Runnable refreshCallback) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle("Завантаження фото для подорожі ID: " + journeyId);

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label fileLabel = new Label("Файл фото:");
    fileLabel.setStyle(LABEL_STYLE);
    grid.add(fileLabel, 0, rowIndex);
    TextField filePathField = new TextField();
    filePathField.setEditable(false);
    filePathField.setStyle(INPUT_STYLE);
    Button chooseFileBtn = new Button("Обрати файл...");
    chooseFileBtn.setStyle(BUTTON_STYLE_SECONDARY);
    HBox fileBox = new HBox(5, filePathField, chooseFileBtn);
    grid.add(fileBox, 1, rowIndex++);

    final File[] selectedFile = new File[1];
    chooseFileBtn.setOnAction(
        e -> {
          FileChooser fileChooser = new FileChooser();
          fileChooser.setTitle("Оберіть фото для завантаження");
          fileChooser
              .getExtensionFilters()
              .addAll(
                  new FileChooser.ExtensionFilter(
                      "Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                  new FileChooser.ExtensionFilter("Всі файли", "*.*"));
          File file = fileChooser.showOpenDialog(formStage);
          if (file != null) {
            filePathField.setText(file.getAbsolutePath());
            selectedFile[0] = file;
          }
        });

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea = new TextArea();
    descriptionArea.setStyle(INPUT_STYLE);
    descriptionArea.setWrapText(true);
    descriptionArea.setPrefRowCount(3);
    grid.add(descriptionArea, 1, rowIndex++);

    Button saveBtn = new Button("Завантажити");
    saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
    HBox buttons = new HBox(10, cancelBtn, saveBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(buttons, 1, rowIndex);

    saveBtn.setOnAction(
        e -> {
          if (selectedFile[0] == null) {
            showAlert(
                Alert.AlertType.WARNING, "Файл не обрано", "Будь ласка, оберіть файл фотографії.");
            return;
          }
          String description = descriptionArea.getText();
          PhotoUploadDto dto =
              new PhotoUploadDto(journeyId, selectedFile[0].getAbsolutePath(), description);
          Set<ConstraintViolation<PhotoUploadDto>> violations = validator.validate(dto);
          if (!violations.isEmpty()) {
            showAlert(
                Alert.AlertType.ERROR,
                "Помилка валідації",
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n")));
            return;
          }

          try {
            photoService.uploadPhoto(dto, currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Фотографію завантажено.");
            if (refreshCallback != null) {
              refreshCallback.run();
            }
            formStage.close();
          } catch (SecurityException secEx) {
            showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка завантаження", ex.getMessage());
            ex.printStackTrace();
          }
        });
    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 500, 300);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  private void showTagFormScene(Tag tagToEdit, ListView<Tag> tagListView) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(tagToEdit == null ? "Створення нового тегу" : "Редагування тегу");

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label nameLabel = new Label("Назва тегу:");
    nameLabel.setStyle(LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField(tagToEdit != null ? tagToEdit.getName() : "");
    nameField.setStyle(INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
    HBox buttons = new HBox(10, cancelBtn, saveBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(buttons, 1, rowIndex);

    saveBtn.setOnAction(
        e -> {
          String name = nameField.getText();
          TagCreationDto dto = new TagCreationDto(name);
          Set<ConstraintViolation<TagCreationDto>> violations = validator.validate(dto);
          if (!violations.isEmpty()) {
            showAlert(
                Alert.AlertType.ERROR,
                "Помилка валідації",
                violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n")));
            return;
          }
          try {
            if (tagToEdit == null) {
              Tag newTag = tagService.createTag(dto.getName());
              showAlert(
                  Alert.AlertType.INFORMATION, "Успіх", "Тег '" + newTag.getName() + "' створено.");
            } else {
              Tag updatedTag = tagService.updateTag(tagToEdit.getId(), dto.getName());
              showAlert(
                  Alert.AlertType.INFORMATION,
                  "Успіх",
                  "Тег '" + updatedTag.getName() + "' оновлено.");
            }
            formStage.close();
            refreshTagList(tagListView, "");
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
          }
        });
    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 400, 200);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  // --- Методи оновлення списків ---

  private void refreshJourneyList(ListView<Journey> listView, String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Journey> sourceList;
    if (showingAllJourneys.get()) {
      try {
        sourceList = FXCollections.observableArrayList(journeyService.getAllJourneys());
      } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список подорожей.");
        sourceList = FXCollections.observableArrayList();
      }
    } else {
      try {
        sourceList =
            FXCollections.observableArrayList(
                journeyService.getJourneysByUserId(currentUser.getId()));
      } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити ваші подорожі.");
        sourceList = FXCollections.observableArrayList();
      }
    }
    if (currentSearchText.isEmpty()) {
      listView.setItems(sourceList);
    } else {
      listView.setItems(
          sourceList.stream()
              .filter(
                  journey ->
                      (journey.getName() != null
                              && journey.getName().toLowerCase().contains(currentSearchText))
                          || (journey.getDescription() != null
                              && journey
                                  .getDescription()
                                  .toLowerCase()
                                  .contains(currentSearchText)))
              .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }
  }

  private void refreshEventList(ListView<Event> listView, String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Event> sourceList;
    try {
      sourceList = FXCollections.observableArrayList(eventService.getAllEvents());
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список подій.");
      sourceList = FXCollections.observableArrayList();
    }
    if (currentSearchText.isEmpty()) {
      listView.setItems(sourceList);
    } else {
      listView.setItems(
          sourceList.stream()
              .filter(
                  event ->
                      (event.getName() != null
                              && event.getName().toLowerCase().contains(currentSearchText))
                          || (event.getDescription() != null
                              && event.getDescription().toLowerCase().contains(currentSearchText)))
              .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }
  }

  private void refreshTagList(ListView<Tag> listView, String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Tag> sourceList;
    try {
      sourceList = FXCollections.observableArrayList(tagService.getAllTags());
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список тегів.");
      sourceList = FXCollections.observableArrayList();
    }
    if (currentSearchText.isEmpty()) {
      listView.setItems(sourceList);
    } else {
      listView.setItems(
          sourceList.stream()
              .filter(
                  tag ->
                      tag.getName() != null
                          && tag.getName().toLowerCase().contains(currentSearchText))
              .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }
  }

  private void refreshLocationList(ListView<Location> listView, String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Location> sourceList;
    try {
      sourceList = FXCollections.observableArrayList(locationService.getAllLocations());
    } catch (Exception e) {
      showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список локацій.");
      sourceList = FXCollections.observableArrayList();
    }
    if (currentSearchText.isEmpty()) {
      listView.setItems(sourceList);
    } else {
      listView.setItems(
          sourceList.stream()
              .filter(
                  loc ->
                      (loc.getName() != null
                              && loc.getName().toLowerCase().contains(currentSearchText))
                          || (loc.getDescription() != null
                              && loc.getDescription().toLowerCase().contains(currentSearchText)))
              .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }
  }

  private void refreshPhotoTilePane(TextField journeyIdField, TilePane photoTilePane) {
    String journeyIdText = journeyIdField.getText();
    if (journeyIdText == null || journeyIdText.trim().isEmpty()) {
      photoTilePane.getChildren().clear();
      return;
    }

    try {
      long journeyId = Long.parseLong(journeyIdText);
      photoTilePane.getChildren().clear();

      photoService
          .getPhotosByJourneyId(journeyId)
          .forEach(
              photo -> {
                try {
                  File imgFile = new File(photo.getFilePath());
                  if (imgFile.exists() && !imgFile.isDirectory()) {
                    Image image =
                        new Image(new FileInputStream(photo.getFilePath()), 150, 150, true, true);
                    ImageView imageView = new ImageView(image);

                    Label descLabel =
                        new Label(
                            photo.getDescription() != null && !photo.getDescription().isEmpty()
                                ? photo.getDescription()
                                : "Без опису");
                    descLabel.setTextFill(Color.WHITE);
                    descLabel.setMaxWidth(140);
                    descLabel.setWrapText(true);
                    descLabel.setAlignment(Pos.CENTER);

                    Button deletePhotoButton = new Button("Видалити");
                    deletePhotoButton.setStyle(BUTTON_STYLE_DANGER);
                    deletePhotoButton.setOnAction(
                        delEvent -> {
                          Alert confirm =
                              new Alert(
                                  Alert.AlertType.CONFIRMATION,
                                  "Видалити цю фотографію?",
                                  ButtonType.YES,
                                  ButtonType.NO);
                          applyDialogStyles(confirm.getDialogPane());
                          Optional<ButtonType> result = confirm.showAndWait();
                          if (result.isPresent() && result.get() == ButtonType.YES) {
                            try {
                              photoService.deletePhoto(photo.getId(), currentUser);
                              refreshPhotoTilePane(journeyIdField, photoTilePane);
                              showAlert(
                                  Alert.AlertType.INFORMATION, "Успіх", "Фотографію видалено.");
                            } catch (SecurityException secEx) {
                              showAlert(
                                  Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                            } catch (Exception ex) {
                              showAlert(
                                  Alert.AlertType.ERROR, "Помилка видалення", ex.getMessage());
                            }
                          }
                        });
                    Optional<Journey> journeyOpt =
                        journeyService.getJourneyById(photo.getJourneyId());
                    deletePhotoButton.setDisable(
                        !journeyOpt
                            .map(j -> j.getUserId().equals(currentUser.getId()))
                            .orElse(true));

                    VBox photoBox = new VBox(5, imageView, descLabel, deletePhotoButton);
                    photoBox.setAlignment(Pos.CENTER);
                    photoBox.setPadding(new Insets(5));
                    photoBox.setStyle(
                        "-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: #3c3f41; -fx-background-radius: 5; -fx-min-width: 160; -fx-max-width: 160;");
                    photoTilePane.getChildren().add(photoBox);
                  } else {
                    System.err.println(
                        "Файл фото не знайдено або є директорією: " + photo.getFilePath());
                  }
                } catch (FileNotFoundException fnfEx) {
                  System.err.println("Файл фото не знайдено: " + photo.getFilePath());
                } catch (Exception ex) {
                  System.err.println(
                      "Помилка завантаження зображення: "
                          + photo.getFilePath()
                          + " - "
                          + ex.getMessage());
                }
              });
    } catch (NumberFormatException ex) {
      showAlert(Alert.AlertType.ERROR, "Помилка", "ID подорожі має бути числовим значенням.");
    } catch (Exception ex) {
      showAlert(
          Alert.AlertType.ERROR, "Помилка завантаження", "Не вдалося завантажити фотографії.");
      ex.printStackTrace();
    }
  }

  // --- Допоміжні методи ---
  private GridPane createGridPane() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.TOP_LEFT);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));
    grid.setStyle("-fx-background-color: #2b2b2b;");

    // Налаштування стовпців для кращого відображення міток
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.NEVER); // Мітки не будуть розширюватися
    col1.setHalignment(HPos.RIGHT); // Вирівнювання міток по правому краю (опціонально)

    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS); // Поля введення будуть розширюватися

    grid.getColumnConstraints().addAll(col1, col2);
    return grid;
  }

  private void applyDialogStyles(DialogPane dialogPane) {
    dialogPane.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    dialogPane.setStyle("-fx-background-color: #2b2b2b;");
    Node content = dialogPane.lookup(".content.label");
    if (content instanceof Label) {
      ((Label) content).setTextFill(Color.WHITE);
    }
    dialogPane
        .getButtonTypes()
        .forEach(
            buttonType -> {
              Button button = (Button) dialogPane.lookupButton(buttonType);
              if (button != null) {
                button.setStyle(BUTTON_STYLE_SECONDARY);
              }
            });
  }

  private void showAlert(Alert.AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    applyDialogStyles(alert.getDialogPane());
    alert.showAndWait();
  }

  private LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalDate.parse(dateString, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      showAlert(
          Alert.AlertType.WARNING,
          "Некоректна дата",
          "Формат дати має бути дд.мм.рррр. Поле буде проігноровано.");
      return null;
    }
  }

  private LocalTime parseTime(String timeString) {
    if (timeString == null || timeString.trim().isEmpty()) {
      return null;
    }
    try {
      return LocalTime.parse(timeString, TIME_FORMATTER);
    } catch (DateTimeParseException e) {
      showAlert(
          Alert.AlertType.WARNING,
          "Некоректний час",
          "Формат часу має бути гг:хх (наприклад, 14:30). Поле буде проігноровано.");
      return null;
    }
  }
}
