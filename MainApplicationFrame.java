package com.trailtales.ui;

import atlantafx.base.theme.PrimerDark;
import com.trailtales.dto.EventCreationDto;
import com.trailtales.dto.JourneyCreationDto;
import com.trailtales.dto.JourneyUpdateDto;
import com.trailtales.dto.LocationCreationDto;
import com.trailtales.dto.PhotoUploadDto;
import com.trailtales.dto.TagCreationDto;
import com.trailtales.entity.Event;
import com.trailtales.entity.Journey;
import com.trailtales.entity.Location;
import com.trailtales.entity.Photo;
import com.trailtales.entity.Tag;
import com.trailtales.entity.User;
import com.trailtales.service.EventService;
import com.trailtales.service.JourneyService;
import com.trailtales.service.LocationService;
import com.trailtales.service.PhotoService;
import com.trailtales.service.TagService;
import com.trailtales.service.UserService;
import com.trailtales.ui.managers.EventViewManager;
import com.trailtales.ui.managers.JourneyViewManager;
import com.trailtales.ui.managers.LocationViewManager;
import com.trailtales.ui.managers.PhotoViewManager;
import com.trailtales.ui.managers.TagViewManager;
import com.trailtales.ui.util.UIConstants; // Залишаємо UIConstants
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List; // Потрібно для Function<List<String>...
import java.util.Set;
import java.util.function.BiFunction; // Потрібно для createDetailRow
import java.util.function.Function; // Потрібно для createListViewForDetails
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
  private Stage primaryStage;
  private BorderPane mainLayout;
  private final Runnable logoutCallback;

  private final JourneyViewManager journeyViewManager;
  private final EventViewManager eventViewManager;
  private final TagViewManager tagViewManager;
  private final LocationViewManager locationViewManager;
  private final PhotoViewManager photoViewManager;

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

  public MainApplicationFrame(
      ApplicationContext context, User currentUser, Runnable logoutCallback, Stage primaryStage) {
    this.userService = context.getBean(UserService.class);
    this.eventService = context.getBean(EventService.class);
    this.journeyService = context.getBean(JourneyService.class);
    this.tagService = context.getBean(TagService.class);
    this.locationService = context.getBean(LocationService.class);
    this.photoService = context.getBean(PhotoService.class);
    this.validator = context.getBean(Validator.class);
    this.currentUser = currentUser;
    this.logoutCallback = logoutCallback;
    this.primaryStage = primaryStage;

    this.journeyViewManager = new JourneyViewManager(this.journeyService, this.currentUser, this);
    this.eventViewManager =
        new EventViewManager(this.eventService, this.journeyService, this.currentUser, this);
    this.tagViewManager = new TagViewManager(this.tagService, this.currentUser, this);
    this.locationViewManager =
        new LocationViewManager(this.locationService, this.currentUser, this);
    this.photoViewManager =
        new PhotoViewManager(this.photoService, this.journeyService, this.currentUser, this);
  }

  public Scene createMainScene() {
    mainLayout = new BorderPane();
    mainLayout.setStyle(UIConstants.BACKGROUND_STYLE_MAIN_APP);

    VBox sideBar = createSideBar();
    mainLayout.setLeft(sideBar);

    Pane initialContent = journeyViewManager.createJourneyPane();
    mainLayout.setCenter(initialContent);
    if (!sideBar.getChildren().isEmpty() && sideBar.getChildren().get(1) instanceof Button) {
      setActiveSidebarButton((Button) sideBar.getChildren().get(1));
    }
    return new Scene(mainLayout, 1200, 800);
  }

  private Button createSidebarButton(String text) {
    Button button = new Button(text);
    button.setStyle(UIConstants.SIDEBAR_BUTTON_STYLE);
    button.setPrefWidth(Double.MAX_VALUE);
    button.setOnMouseEntered(
        e -> {
          if (!button.getProperties().containsKey("active")) {
            button.setStyle(
                UIConstants.SIDEBAR_BUTTON_STYLE_ACTIVE.replace(
                    "-fx-border-color: #007bff;", "-fx-border-color: #555;"));
          }
        });
    button.setOnMouseExited(
        e -> {
          if (!button.getProperties().containsKey("active")) {
            button.setStyle(UIConstants.SIDEBAR_BUTTON_STYLE);
          }
        });
    return button;
  }

  private VBox createSideBar() {
    VBox sideBar = new VBox(0);
    sideBar.setPadding(new Insets(10, 0, 10, 0));
    sideBar.setStyle(UIConstants.BACKGROUND_STYLE_SIDEBAR);
    sideBar.setPrefWidth(220);

    Text appTitle = new Text("TrailTales");
    appTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
    appTitle.setFill(Color.WHITE);
    HBox titleBox = new HBox(appTitle);
    titleBox.setAlignment(Pos.CENTER);
    titleBox.setPadding(new Insets(15, 0, 20, 0));
    sideBar.getChildren().add(titleBox);

    Button journeysBtn = createSidebarButton("Подорожі");
    journeysBtn.setOnAction(
        e -> {
          mainLayout.setCenter(journeyViewManager.createJourneyPane());
          setActiveSidebarButton(journeysBtn);
        });

    Button eventsBtn = createSidebarButton("Події");
    eventsBtn.setOnAction(
        e -> {
          mainLayout.setCenter(eventViewManager.createEventPane());
          setActiveSidebarButton(eventsBtn);
        });

    Button tagsBtn = createSidebarButton("Теги");
    tagsBtn.setOnAction(
        e -> {
          mainLayout.setCenter(tagViewManager.createTagPane());
          setActiveSidebarButton(tagsBtn);
        });

    Button locationsBtn = createSidebarButton("Локації");
    locationsBtn.setOnAction(
        e -> {
          mainLayout.setCenter(locationViewManager.createLocationPane());
          setActiveSidebarButton(locationsBtn);
        });

    Button photosBtn = createSidebarButton("Фотографії");
    photosBtn.setOnAction(
        e -> {
          mainLayout.setCenter(photoViewManager.createPhotoPane());
          setActiveSidebarButton(photosBtn);
        });

    Button logoutBtn = createSidebarButton("Вийти");
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

  private void setActiveSidebarButton(Button activeButton) {
    if (mainLayout.getLeft() instanceof VBox) {
      VBox sideBar = (VBox) mainLayout.getLeft();
      sideBar
          .getChildren()
          .forEach(
              node -> {
                if (node instanceof Button) {
                  node.getProperties().remove("active");
                  node.setStyle(UIConstants.SIDEBAR_BUTTON_STYLE);
                }
              });
      activeButton.setStyle(UIConstants.SIDEBAR_BUTTON_STYLE_ACTIVE);
      activeButton.getProperties().put("active", true);
    }
  }

  public void showJourneyFormScene(Journey journeyToEdit, JourneyViewManager manager) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(journeyToEdit == null ? "Створення нової подорожі" : "Редагування подорожі");

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label nameLabel = new Label("Назва:");
    nameLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField();
    nameField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea = new TextArea();
    descriptionArea.setStyle(UIConstants.INPUT_STYLE);
    descriptionArea.setWrapText(true);
    grid.add(descriptionArea, 1, rowIndex++);

    Label startDateLabel = new Label("Дата початку (дд.мм.рррр):");
    startDateLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(startDateLabel, 0, rowIndex);
    DatePicker startDatePicker = new DatePicker();
    startDatePicker.setStyle(UIConstants.INPUT_STYLE);
    grid.add(startDatePicker, 1, rowIndex++);

    Label endDateLabel = new Label("Дата кінця (дд.мм.рррр):");
    endDateLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(endDateLabel, 0, rowIndex);
    DatePicker endDatePicker = new DatePicker();
    endDatePicker.setStyle(UIConstants.INPUT_STYLE);
    grid.add(endDatePicker, 1, rowIndex++);

    Label originLocLabel = new Label("Початкова локація:");
    originLocLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(originLocLabel, 0, rowIndex);
    TextField originLocationField = new TextField();
    originLocationField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(originLocationField, 1, rowIndex++);

    Label originLocDescLabel = new Label("Опис початкової локації:");
    originLocDescLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(originLocDescLabel, 0, rowIndex);
    TextField originLocationDescField = new TextField();
    originLocationDescField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(originLocationDescField, 1, rowIndex++);

    Label destLocLabel = new Label("Кінцева локація:");
    destLocLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(destLocLabel, 0, rowIndex);
    TextField destLocationField = new TextField();
    destLocationField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(destLocationField, 1, rowIndex++);

    Label destLocDescLabel = new Label("Опис кінцевої локації:");
    destLocDescLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(destLocDescLabel, 0, rowIndex);
    TextField destLocationDescField = new TextField();
    destLocationDescField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(destLocationDescField, 1, rowIndex++);

    Label tagsLabel = new Label("Теги (через кому):");
    tagsLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(tagsLabel, 0, rowIndex);
    TextField tagsField = new TextField();
    tagsField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(tagsField, 1, rowIndex++);

    VBox participantsContainer = new VBox(5);
    participantsContainer.setPadding(new Insets(5));

    Label participantsListLabel = new Label("Список учасників:");
    participantsListLabel.setStyle(UIConstants.LABEL_STYLE.replace("-fx-min-width: 150px;", ""));
    ListView<String> participantsListView = new ListView<>();
    participantsListView.setPrefHeight(80);
    participantsListView.setStyle(UIConstants.LIST_VIEW_STYLE);

    HBox participantManagementBox = new HBox(10);
    participantManagementBox.setAlignment(Pos.CENTER_LEFT);
    TextField participantIdentifierField = new TextField();
    participantIdentifierField.setPromptText("Username або Email учасника");
    participantIdentifierField.setStyle(UIConstants.INPUT_STYLE);
    HBox.setHgrow(participantIdentifierField, Priority.ALWAYS);

    Button addParticipantButton = new Button("Додати");
    addParticipantButton.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button removeParticipantButton = new Button("Видалити");
    removeParticipantButton.setStyle(UIConstants.BUTTON_STYLE_DANGER);
    removeParticipantButton.setDisable(true);

    participantManagementBox
        .getChildren()
        .addAll(participantIdentifierField, addParticipantButton, removeParticipantButton);
    participantsContainer
        .getChildren()
        .addAll(participantsListLabel, participantsListView, participantManagementBox);

    TitledPane participantsTitledPane = new TitledPane("Учасники подорожі", participantsContainer);
    participantsTitledPane.setCollapsible(true);
    participantsTitledPane.setExpanded(false);
    participantsTitledPane.setTextFill(Color.WHITE);
    participantsTitledPane.setStyle("-fx-font-weight: bold;");

    grid.add(participantsTitledPane, 0, rowIndex++, 2, 1);

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
      if (journeyToEdit.getParticipants() != null && !journeyToEdit.getParticipants().isEmpty()) {
        participantsListView.setItems(
            FXCollections.observableArrayList(
                journeyToEdit.getParticipants().stream()
                    .map(User::getUsername)
                    .collect(Collectors.toList())));
        participantsTitledPane.setExpanded(true);
      }
    } else {
      addParticipantButton.setDisable(true);
      participantIdentifierField.setDisable(true);
      participantsTitledPane.setExpanded(false);
      Label infoLabel = new Label("Керування учасниками буде доступне після збереження подорожі.");
      infoLabel.setStyle(UIConstants.DETAILED_LABEL_STYLE);
      participantsContainer.getChildren().add(infoLabel);
    }

    participantsListView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldSelection, newSelection) -> {
              removeParticipantButton.setDisable(newSelection == null || journeyToEdit == null);
            });

    addParticipantButton.setOnAction(
        e -> {
          if (journeyToEdit == null) {
            showAlert(
                Alert.AlertType.WARNING,
                "Збережіть подорож",
                "Спочатку збережіть подорож, щоб додати учасників.");
            return;
          }
          String identifier = participantIdentifierField.getText();
          if (identifier == null || identifier.trim().isEmpty()) {
            showAlert(
                Alert.AlertType.WARNING,
                "Порожнє поле",
                "Введіть ім'я користувача або email учасника.");
            return;
          }
          try {
            Journey updatedJourney =
                journeyService.addParticipantToJourney(
                    journeyToEdit.getId(), identifier, currentUser);
            journeyToEdit.setParticipants(updatedJourney.getParticipants());
            participantsListView.setItems(
                FXCollections.observableArrayList(
                    updatedJourney.getParticipants().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList())));
            participantIdentifierField.clear();
            showAlert(
                Alert.AlertType.INFORMATION, "Успіх", "Учасника '" + identifier + "' додано.");
          } catch (IllegalArgumentException | SecurityException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка додавання", ex.getMessage());
          }
        });

    removeParticipantButton.setOnAction(
        e -> {
          if (journeyToEdit == null) return;
          String selectedUsername = participantsListView.getSelectionModel().getSelectedItem();
          if (selectedUsername == null) {
            showAlert(
                Alert.AlertType.WARNING,
                "Учасника не обрано",
                "Оберіть учасника зі списку для видалення.");
            return;
          }
          try {
            Journey updatedJourney =
                journeyService.removeParticipantFromJourney(
                    journeyToEdit.getId(), selectedUsername, currentUser);
            journeyToEdit.setParticipants(updatedJourney.getParticipants());
            participantsListView.setItems(
                FXCollections.observableArrayList(
                    updatedJourney.getParticipants().stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList())));
            showAlert(
                Alert.AlertType.INFORMATION,
                "Успіх",
                "Учасника '" + selectedUsername + "' видалено.");
          } catch (IllegalArgumentException | SecurityException ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка видалення", ex.getMessage());
          }
        });

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    HBox journeyActionButtons = new HBox(10, cancelBtn, saveBtn);
    journeyActionButtons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(journeyActionButtons, 1, rowIndex);

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
                  "Подорож '" + dto.getName() + "' створено. Тепер можна додати учасників.");
              formStage.close();
              manager.refreshJourneyList(manager.getCurrentSearchText());

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
              formStage.close();
              manager.refreshJourneyList(manager.getCurrentSearchText());
            }
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
            ex.printStackTrace();
          }
        });
    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 650, 700);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  public void showJourneyDetailsDialog(Journey journey) {
    Stage dialogStage = new Stage();
    dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initOwner(primaryStage);
    dialogStage.setTitle("Деталі подорожі: " + journey.getName());

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setStyle(UIConstants.BACKGROUND_STYLE_SIDEBAR);

    BiFunction<String, String, HBox> createDetailRow =
        (labelText, valueText) -> {
          Label label = new Label(labelText + ":");
          label.setStyle(
              UIConstants.LABEL_STYLE.replace(
                  "-fx-min-width: 150px;", "-fx-font-weight: bold; -fx-text-fill: #b0b0b0;"));
          Text value = new Text(valueText != null ? valueText : "N/A");
          value.setStyle(UIConstants.VALUE_TEXT_STYLE);
          value.setWrappingWidth(350);
          HBox row = new HBox(5, label, value);
          row.setAlignment(Pos.CENTER_LEFT);
          return row;
        };

    Function<List<String>, ListView<String>> createListViewForDetails =
        (items) -> {
          ListView<String> listView = new ListView<>();
          if (items != null && !items.isEmpty()) {
            listView.setItems(FXCollections.observableArrayList(items));
          } else {
            listView.setItems(FXCollections.observableArrayList("Немає даних"));
          }
          listView.setPrefHeight(80);
          listView.setStyle(UIConstants.LIST_VIEW_STYLE);
          return listView;
        };

    layout.getChildren().add(createDetailRow.apply("ID Подорожі", journey.getId().toString()));
    layout.getChildren().add(createDetailRow.apply("Назва", journey.getName()));
    layout.getChildren().add(createDetailRow.apply("Опис", journey.getDescription()));
    if (journey.getUser() != null) {
      layout.getChildren().add(createDetailRow.apply("Власник", journey.getUser().getUsername()));
    }
    layout
        .getChildren()
        .add(
            createDetailRow.apply(
                "Дата початку",
                journey.getStartDate() != null
                    ? DATE_FORMATTER.format(journey.getStartDate())
                    : "N/A"));
    layout
        .getChildren()
        .add(
            createDetailRow.apply(
                "Дата завершення",
                journey.getEndDate() != null
                    ? DATE_FORMATTER.format(journey.getEndDate())
                    : "N/A"));

    if (journey.getOriginLocation() != null) {
      layout
          .getChildren()
          .add(
              createDetailRow.apply(
                  "Початкова локація",
                  journey.getOriginLocation().getName()
                      + (journey.getOriginLocation().getDescription() != null
                              && !journey.getOriginLocation().getDescription().isEmpty()
                          ? " (" + journey.getOriginLocation().getDescription() + ")"
                          : "")));
    } else {
      layout.getChildren().add(createDetailRow.apply("Початкова локація", "N/A"));
    }

    if (journey.getDestinationLocation() != null) {
      layout
          .getChildren()
          .add(
              createDetailRow.apply(
                  "Кінцева локація",
                  journey.getDestinationLocation().getName()
                      + (journey.getDestinationLocation().getDescription() != null
                              && !journey.getDestinationLocation().getDescription().isEmpty()
                          ? " (" + journey.getDestinationLocation().getDescription() + ")"
                          : "")));
    } else {
      layout.getChildren().add(createDetailRow.apply("Кінцева локація", "N/A"));
    }

    Label tagsLabel = new Label("Теги:");
    tagsLabel.setStyle(
        UIConstants.LABEL_STYLE.replace(
            "-fx-min-width: 150px;", "-fx-font-weight: bold; -fx-text-fill: #b0b0b0;"));
    layout.getChildren().add(tagsLabel);
    if (journey.getTags() != null && !journey.getTags().isEmpty()) {
      String tagsString =
          journey.getTags().stream().map(Tag::getName).collect(Collectors.joining(", "));
      Text tagsValue = new Text(tagsString);
      tagsValue.setStyle(UIConstants.VALUE_TEXT_STYLE);
      tagsValue.setWrappingWidth(450);
      layout.getChildren().add(tagsValue);
    } else {
      layout.getChildren().add(new Text("Немає тегів"));
    }

    Label participantsLabel = new Label("Учасники:");
    participantsLabel.setStyle(
        UIConstants.LABEL_STYLE.replace(
            "-fx-min-width: 150px;", "-fx-font-weight: bold; -fx-text-fill: #b0b0b0;"));
    layout.getChildren().add(participantsLabel);
    if (journey.getParticipants() != null && !journey.getParticipants().isEmpty()) {
      layout
          .getChildren()
          .add(
              createListViewForDetails.apply(
                  journey.getParticipants().stream()
                      .map(User::getUsername)
                      .collect(Collectors.toList())));
    } else {
      layout.getChildren().add(new Text("Немає учасників"));
    }

    Label eventsLabel = new Label("Події:");
    eventsLabel.setStyle(
        UIConstants.LABEL_STYLE.replace(
            "-fx-min-width: 150px;", "-fx-font-weight: bold; -fx-text-fill: #b0b0b0;"));
    layout.getChildren().add(eventsLabel);
    if (journey.getEvents() != null && !journey.getEvents().isEmpty()) {
      layout
          .getChildren()
          .add(
              createListViewForDetails.apply(
                  journey.getEvents().stream()
                      .map(
                          event ->
                              event.getName()
                                  + (event.getEventDate() != null
                                      ? " (" + DATE_FORMATTER.format(event.getEventDate()) + ")"
                                      : ""))
                      .collect(Collectors.toList())));
    } else {
      layout.getChildren().add(new Text("Немає подій"));
    }

    Label photosLabel = new Label("Фотографії:");
    photosLabel.setStyle(
        UIConstants.LABEL_STYLE.replace(
            "-fx-min-width: 150px;", "-fx-font-weight: bold; -fx-text-fill: #b0b0b0;"));
    layout.getChildren().add(photosLabel);
    if (journey.getPhotos() != null && !journey.getPhotos().isEmpty()) {
      layout
          .getChildren()
          .add(
              createListViewForDetails.apply(
                  journey.getPhotos().stream()
                      .map(
                          photo ->
                              photo.getFilePath()
                                  + (photo.getDescription() != null
                                          && !photo.getDescription().isEmpty()
                                      ? " - " + photo.getDescription()
                                      : ""))
                      .collect(Collectors.toList())));
    } else {
      layout.getChildren().add(new Text("Немає фотографій"));
    }

    layout
        .getChildren()
        .add(
            createDetailRow.apply(
                "Створено", DATE_FORMATTER.format(journey.getCreatedAt().toLocalDate())));
    layout
        .getChildren()
        .add(
            createDetailRow.apply(
                "Оновлено", DATE_FORMATTER.format(journey.getUpdatedAt().toLocalDate())));

    Button closeButton = new Button("Закрити");
    closeButton.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    closeButton.setOnAction(e -> dialogStage.close());
    HBox buttonBox = new HBox(closeButton);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);
    buttonBox.setPadding(new Insets(10, 0, 0, 0));
    layout.getChildren().add(buttonBox);

    ScrollPane scrollPane = new ScrollPane(layout);
    scrollPane.setFitToWidth(true);
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);

    Scene dialogScene = new Scene(scrollPane, 500, 650);
    dialogScene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    dialogStage.setScene(dialogScene);
    dialogStage.showAndWait();
  }

  public void showEventFormScene(Event eventToEdit, EventViewManager manager) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(eventToEdit == null ? "Створення нової події" : "Редагування події");

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label nameLabel = new Label("Назва:");
    nameLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField();
    nameField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea = new TextArea();
    descriptionArea.setStyle(UIConstants.INPUT_STYLE);
    grid.add(descriptionArea, 1, rowIndex++);

    Label dateLabel = new Label("Дата (дд.мм.рррр):");
    dateLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(dateLabel, 0, rowIndex);
    DatePicker datePicker = new DatePicker();
    datePicker.setStyle(UIConstants.INPUT_STYLE);
    grid.add(datePicker, 1, rowIndex++);

    Label timeLabel = new Label("Час (гг:хх):");
    timeLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(timeLabel, 0, rowIndex);
    TextField timeField = new TextField();
    timeField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(timeField, 1, rowIndex++);

    Label locNameLabel = new Label("Локація (назва):");
    locNameLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(locNameLabel, 0, rowIndex);
    TextField locationNameField = new TextField();
    locationNameField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(locationNameField, 1, rowIndex++);

    Label locDescLabel = new Label("Опис локації:");
    locDescLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(locDescLabel, 0, rowIndex);
    TextField locationDescField = new TextField();
    locationDescField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(locationDescField, 1, rowIndex++);

    Label journeyIdLabelEv = new Label("ID Подорожі (необов'язково):");
    journeyIdLabelEv.setStyle(UIConstants.LABEL_STYLE);
    grid.add(journeyIdLabelEv, 0, rowIndex);
    TextField journeyIdField = new TextField();
    journeyIdField.setStyle(UIConstants.INPUT_STYLE);
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
    saveBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    HBox eventActionButtons = new HBox(10, cancelBtn, saveBtn);
    eventActionButtons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(eventActionButtons, 1, rowIndex);

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

              if (locationNameField.getText() != null
                  && !locationNameField.getText().trim().isEmpty()) {
                String locName = locationNameField.getText().trim();
                String locDesc =
                    locationDescField.getText() != null ? locationDescField.getText().trim() : null;
                Location location =
                    locationService
                        .getLocationByName(locName)
                        .orElseGet(() -> locationService.createLocation(locName, locDesc));
                eventToEdit.setLocationId(location.getId());
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
            manager.refreshEventList(manager.getCurrentSearchText());
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
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 500, 480);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  public void showLocationFormScene(Location locationToEdit, LocationViewManager manager) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(locationToEdit == null ? "Створення нової локації" : "Редагування локації");

    GridPane grid = createGridPane();
    int rowIndex = 0;
    Label nameLabel = new Label("Назва:");
    nameLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField(locationToEdit != null ? locationToEdit.getName() : "");
    nameField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea =
        new TextArea(
            locationToEdit != null && locationToEdit.getDescription() != null
                ? locationToEdit.getDescription()
                : "");
    descriptionArea.setStyle(UIConstants.INPUT_STYLE);
    descriptionArea.setWrapText(true);
    descriptionArea.setPrefRowCount(3);
    grid.add(descriptionArea, 1, rowIndex++);

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    HBox locationActionButtons = new HBox(10, cancelBtn, saveBtn);
    locationActionButtons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(locationActionButtons, 1, rowIndex);

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
            manager.refreshLocationList(manager.getCurrentSearchText());
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
          }
        });
    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 450, 300);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  public void showPhotoUploadFormScene(long journeyId, PhotoViewManager manager) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle("Завантаження фото для подорожі ID: " + journeyId);

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label fileLabel = new Label("Файл фото:");
    fileLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(fileLabel, 0, rowIndex);
    TextField filePathField = new TextField();
    filePathField.setEditable(false);
    filePathField.setStyle(UIConstants.INPUT_STYLE);

    final File[] selectedFile = new File[1];
    Button chooseFileBtn = new Button("Обрати файл...");
    chooseFileBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
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

    HBox fileBox = new HBox(5, filePathField, chooseFileBtn);
    GridPane.setHgrow(filePathField, Priority.ALWAYS);
    grid.add(fileBox, 1, rowIndex++);

    Label descLabel = new Label("Опис:");
    descLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(descLabel, 0, rowIndex);
    TextArea descriptionArea = new TextArea();
    descriptionArea.setStyle(UIConstants.INPUT_STYLE);
    descriptionArea.setWrapText(true);
    descriptionArea.setPrefRowCount(3);
    grid.add(descriptionArea, 1, rowIndex++);

    Button saveBtn = new Button("Завантажити");
    saveBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    HBox photoUploadButtons = new HBox(10, cancelBtn, saveBtn);
    photoUploadButtons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(photoUploadButtons, 1, rowIndex);

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
            manager.refreshPhotoTilePane();
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
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 550, 300);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  public void showTagFormScene(Tag tagToEdit, TagViewManager manager) {
    Stage formStage = new Stage();
    formStage.initModality(Modality.WINDOW_MODAL);
    formStage.initOwner(primaryStage);
    formStage.setTitle(tagToEdit == null ? "Створення нового тегу" : "Редагування тегу");

    GridPane grid = createGridPane();
    int rowIndex = 0;

    Label nameLabel = new Label("Назва тегу:");
    nameLabel.setStyle(UIConstants.LABEL_STYLE);
    grid.add(nameLabel, 0, rowIndex);
    TextField nameField = new TextField(tagToEdit != null ? tagToEdit.getName() : "");
    nameField.setStyle(UIConstants.INPUT_STYLE);
    grid.add(nameField, 1, rowIndex++);

    Button saveBtn = new Button("Зберегти");
    saveBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button cancelBtn = new Button("Скасувати");
    cancelBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    HBox tagActionButtons = new HBox(10, cancelBtn, saveBtn);
    tagActionButtons.setAlignment(Pos.CENTER_RIGHT);
    grid.add(tagActionButtons, 1, rowIndex);

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
            manager.refreshTagList(manager.getCurrentSearchText());
          } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
          }
        });
    cancelBtn.setOnAction(e -> formStage.close());

    ScrollPane scrollPane = new ScrollPane(grid);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);
    Scene scene = new Scene(scrollPane, 400, 200);
    scene.setFill(Color.web("#2b2b2b"));
    scene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    formStage.setScene(scene);
    formStage.showAndWait();
  }

  public void showPhotoDescriptionEditDialog(Photo photo, PhotoViewManager photoManager) {
    Stage dialogStage = new Stage();
    dialogStage.initModality(Modality.WINDOW_MODAL);
    dialogStage.initOwner(primaryStage);
    dialogStage.setTitle("Редагувати опис фото");

    VBox vbox = new VBox(10);
    vbox.setPadding(new Insets(20));
    vbox.setStyle(UIConstants.BACKGROUND_STYLE_SIDEBAR);

    Label currentDescLabel =
        new Label(
            "Поточний опис: "
                + (photo.getDescription() != null ? photo.getDescription() : "Немає"));
    currentDescLabel.setStyle(UIConstants.LABEL_STYLE);
    currentDescLabel.setWrapText(true);

    TextArea newDescriptionArea = new TextArea();
    newDescriptionArea.setPromptText("Введіть новий опис");
    newDescriptionArea.setText(photo.getDescription() != null ? photo.getDescription() : "");
    newDescriptionArea.setStyle(UIConstants.INPUT_STYLE);
    newDescriptionArea.setWrapText(true);

    Button saveButton = new Button("Зберегти опис");
    saveButton.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button cancelButton = new Button("Скасувати");
    cancelButton.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    HBox photoDescButtons = new HBox(10, cancelButton, saveButton);
    photoDescButtons.setAlignment(Pos.CENTER_RIGHT);

    saveButton.setOnAction(
        e -> {
          String newDescription = newDescriptionArea.getText();
          try {
            photoService.updatePhotoDescription(photo.getId(), newDescription, currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Опис фотографії оновлено.");
            photoManager.refreshPhotoTilePane();
            dialogStage.close();
          } catch (SecurityException secEx) {
            showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
          } catch (Exception ex) {
            showAlert(
                Alert.AlertType.ERROR,
                "Помилка оновлення",
                "Не вдалося оновити опис: " + ex.getMessage());
          }
        });
    cancelButton.setOnAction(e -> dialogStage.close());

    vbox.getChildren().addAll(currentDescLabel, newDescriptionArea, photoDescButtons);

    Scene dialogScene = new Scene(vbox, 400, 250);
    dialogScene.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    dialogStage.setScene(dialogScene);
    dialogStage.showAndWait();
  }

  private GridPane createGridPane() {
    GridPane grid = new GridPane();
    grid.setAlignment(Pos.TOP_LEFT);
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20));
    grid.setStyle(UIConstants.BACKGROUND_STYLE_SIDEBAR);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.NEVER);
    col1.setHalignment(HPos.RIGHT);

    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);

    grid.getColumnConstraints().addAll(col1, col2);
    return grid;
  }

  public void applyDialogStyles(DialogPane dialogPane) {
    dialogPane.getStylesheets().add(new PrimerDark().getUserAgentStylesheet());
    dialogPane.setStyle(UIConstants.BACKGROUND_STYLE_SIDEBAR);
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
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE
                    || buttonType.getButtonData() == ButtonBar.ButtonData.YES) {
                  button.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
                } else {
                  button.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
                }
              }
            });
  }

  public void showAlert(Alert.AlertType alertType, String title, String message) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    applyDialogStyles(alert.getDialogPane());
    alert.initOwner(primaryStage);
    alert.showAndWait();
  }

  private LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty()) return null;
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
    if (timeString == null || timeString.trim().isEmpty()) return null;
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
