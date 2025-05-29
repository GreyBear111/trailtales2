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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private static final String INPUT_STYLE = "-fx-background-color: #3c3f41; -fx-text-fill: #e0e0e0; -fx-border-color: #555; -fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 7;";
    private static final String BUTTON_STYLE_PRIMARY = "-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;";
    private static final String BUTTON_STYLE_SECONDARY = "-fx-background-color: #5a5a5a; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;";
    private static final String BUTTON_STYLE_DANGER = "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 6 12;";
    private static final String SIDEBAR_BUTTON_STYLE = "-fx-background-color: transparent; -fx-text-fill: #e0e0e0; -fx-font-size: 14px; -fx-alignment: baseline-left; -fx-padding: 10 15; -fx-border-color: transparent; -fx-border-width: 0 0 0 3px;";
    private static final String SIDEBAR_BUTTON_STYLE_ACTIVE = SIDEBAR_BUTTON_STYLE + "-fx-border-color: #007bff;";
    private static final String TITLE_STYLE = "-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: white;";
    private static final String LABEL_STYLE = "-fx-text-fill: #c0c0c0;";
    private static final String LIST_CELL_STYLE_NORMAL = "-fx-background-color: #2b2b2b; -fx-text-fill: white; -fx-padding: 5px;";
    private static final String LIST_CELL_STYLE_SELECTED = "-fx-background-color: #005c99; -fx-text-fill: white; -fx-padding: 5px;";

    public MainApplicationFrame(ApplicationContext context, User currentUser, Runnable logoutCallback) {
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

    public Scene createMainScene() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #1e1e1e;");

        VBox sideBar = createSideBar();
        mainLayout.setLeft(sideBar);

        Pane initialContent = createJourneyListPane();
        mainLayout.setCenter(initialContent);
        if (!sideBar.getChildren().isEmpty() && sideBar.getChildren().get(1) instanceof Button) {
            setActiveSidebarButton((Button) sideBar.getChildren().get(1));
        }
        // Отримуємо primaryStage з mainLayout, якщо це можливо, або передаємо його
        // Це важливо для коректного встановлення owner для модальних вікон
        if (mainLayout.getScene() != null && mainLayout.getScene().getWindow() instanceof Stage) {
            this.primaryStage = (Stage) mainLayout.getScene().getWindow();
        } else {
            // Якщо сцена ще не встановлена, primaryStage має бути переданий з MainApp
            // У нашому випадку MainApp вже встановлює сцену, тому цей блок може бути зайвим,
            // але для надійності можна залишити або передавати primaryStage в конструктор MainApplicationFrame
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

        journeysBtn.setOnAction(e -> {
            mainLayout.setCenter(createJourneyListPane());
            setActiveSidebarButton(journeysBtn);
        });
        eventsBtn.setOnAction(e -> {
            mainLayout.setCenter(createEventListPane());
            setActiveSidebarButton(eventsBtn);
        });
        tagsBtn.setOnAction(e -> {
            mainLayout.setCenter(createTagListPane());
            setActiveSidebarButton(tagsBtn);
        });
        locationsBtn.setOnAction(e -> {
            mainLayout.setCenter(createLocationListPane());
            setActiveSidebarButton(locationsBtn);
        });
        photosBtn.setOnAction(e -> {
            mainLayout.setCenter(createPhotoListPane());
            setActiveSidebarButton(photosBtn);
        });

        logoutBtn.setOnAction(e -> {
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
        button.setOnMouseExited(e -> {
            if (!button.getProperties().containsKey("active")) {
                button.setStyle(SIDEBAR_BUTTON_STYLE);
            }
        });
        return button;
    }

    private void setActiveSidebarButton(Button activeButton) {
        if (mainLayout.getLeft() instanceof VBox) {
            VBox sideBar = (VBox) mainLayout.getLeft();
            sideBar.getChildren().forEach(node -> {
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
        listView.setCellFactory(lv -> new ListCell<Journey>() {
            @Override
            protected void updateItem(Journey item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                    setText(item.getName() + (showingAllJourneys.get() && item.getUser() != null ? " (Автор: " + item.getUser().getUsername() + ")" : ""));
                    setTextFill(Color.WHITE);
                    if (isSelected()) {
                        setStyle(LIST_CELL_STYLE_SELECTED);
                    } else {
                        setStyle(LIST_CELL_STYLE_NORMAL);
                    }
                }
            }
        });

        ObservableList<Journey> journeys = FXCollections.observableArrayList();

        Runnable updateItems = () -> {
            String currentSearchText = searchField.getText().toLowerCase();
            ObservableList<Journey> sourceList;
            if (showingAllJourneys.get()) {
                try {
                    sourceList = FXCollections.observableArrayList(journeyService.getAllJourneys());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося завантажити всі подорожі.");
                    sourceList = FXCollections.observableArrayList();
                }
            } else {
                try {
                    sourceList = FXCollections.observableArrayList(journeyService.getJourneysByUserId(currentUser.getId()));
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося завантажити ваші подорожі.");
                    sourceList = FXCollections.observableArrayList();
                }
            }

            if (currentSearchText.isEmpty()) {
                journeys.setAll(sourceList);
            } else {
                journeys.setAll(sourceList.stream()
                        .filter(journey -> (journey.getName() != null && journey.getName().toLowerCase().contains(currentSearchText)) ||
                                (journey.getDescription() != null && journey.getDescription().toLowerCase().contains(currentSearchText)))
                        .collect(Collectors.toList()));
            }
            listView.setItems(journeys);
        };

        updateItems.run();

        Button createBtn = new Button("Створити"); createBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button editBtn = new Button("Редагувати"); editBtn.setStyle(BUTTON_STYLE_SECONDARY);
        Button deleteBtn = new Button("Видалити"); deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);
        Button viewAllBtn = new Button("Всі подорожі"); viewAllBtn.setStyle(BUTTON_STYLE_SECONDARY);
        Button myJourneysBtn = new Button("Мої подорожі"); myJourneysBtn.setStyle(BUTTON_STYLE_SECONDARY);

        if (currentUser == null || !currentUser.getRoles().stream().anyMatch(role -> role.getName() == RoleName.ROLE_ADMIN)) {
            viewAllBtn.setVisible(false); viewAllBtn.setManaged(false);
        }

        editBtn.setDisable(true); deleteBtn.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isSelected = newSelection != null;
            boolean canEditOrDelete = isSelected && currentUser != null && newSelection.getUserId().equals(currentUser.getId());
            editBtn.setDisable(!canEditOrDelete);
            deleteBtn.setDisable(!canEditOrDelete);
        });

        createBtn.setOnAction(e -> showJourneyFormScene(null));
        editBtn.setOnAction(e -> {
            Journey selectedJourney = listView.getSelectionModel().getSelectedItem();
            if (selectedJourney != null) {
                showJourneyFormScene(selectedJourney);
            }
        });
        deleteBtn.setOnAction(e -> {
            Journey selectedJourney = listView.getSelectionModel().getSelectedItem();
            if (selectedJourney != null) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, "Ви впевнені, що хочете видалити подорож '" + selectedJourney.getName() + "'?", ButtonType.YES, ButtonType.NO);
                confirmAlert.setTitle("Підтвердження видалення");
                confirmAlert.setHeaderText(null);
                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.YES) {
                    try {
                        journeyService.deleteJourney(selectedJourney.getId(), currentUser);
                        updateItems.run();
                        showAlert(Alert.AlertType.INFORMATION, "Успіх", "Подорож видалено.");
                    } catch (SecurityException secEx) {
                        showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Помилка видалення", "Не вдалося видалити подорож.");
                        ex.printStackTrace();
                    }
                }
            }
        });
        viewAllBtn.setOnAction(e -> {
            showingAllJourneys.set(true);
            updateItems.run();
        });

        myJourneysBtn.setOnAction(e -> {
            showingAllJourneys.set(false);
            updateItems.run();
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateItems.run());

        HBox filterButtons = new HBox(10, myJourneysBtn, viewAllBtn);
        filterButtons.setAlignment(Pos.CENTER_LEFT);
        HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
        crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(10);
        controlsGrid.add(searchField, 0,0);
        controlsGrid.add(filterButtons, 1,0);
        controlsGrid.add(crudButtonsBox, 2,0);
        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.SOMETIMES);
        ColumnConstraints col3 = new ColumnConstraints(); col3.setHgrow(Priority.SOMETIMES);
        controlsGrid.getColumnConstraints().addAll(col1,col2,col3);

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
        ObservableList<Event> events = FXCollections.observableArrayList();

        Runnable updateEventItems = () -> {
            String currentSearchText = searchField.getText().toLowerCase();
            ObservableList<Event> sourceList;
            try {
                sourceList = FXCollections.observableArrayList(eventService.getAllEvents());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося завантажити події.");
                sourceList = FXCollections.observableArrayList();
            }
            if (currentSearchText.isEmpty()) {
                events.setAll(sourceList);
            } else {
                events.setAll(sourceList.stream()
                        .filter(event -> (event.getName() != null && event.getName().toLowerCase().contains(currentSearchText)) ||
                                (event.getDescription() != null && event.getDescription().toLowerCase().contains(currentSearchText)))
                        .collect(Collectors.toList()));
            }
            listView.setItems(events);
        };
        updateEventItems.run();

        listView.setCellFactory(lv -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                    String journeyName = "";
                    if (item.getJourneyId() != null) {
                        Optional<Journey> journeyOpt = journeyService.getJourneyById(item.getJourneyId());
                        journeyName = journeyOpt.map(j -> " (Подорож: " + j.getName() + ")").orElse("");
                    }
                    setText(item.getName() + (item.getEventDate() != null ? " [" + DATE_FORMATTER.format(item.getEventDate()) + "]" : "") + journeyName);
                    setTextFill(Color.WHITE);
                    if (isSelected()) {
                        setStyle(LIST_CELL_STYLE_SELECTED);
                    } else {
                        setStyle(LIST_CELL_STYLE_NORMAL);
                    }
                }
            }
        });

        Button createBtn = new Button("Створити"); createBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button editBtn = new Button("Редагувати"); editBtn.setStyle(BUTTON_STYLE_SECONDARY);
        Button deleteBtn = new Button("Видалити"); deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);

        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean canModify = false;
            if (newVal != null && currentUser != null) {
                if (newVal.getJourneyId() != null) {
                    Optional<Journey> journeyOpt = journeyService.getJourneyById(newVal.getJourneyId());
                    canModify = journeyOpt.map(j -> j.getUserId().equals(currentUser.getId())).orElse(false);
                } else {
                    canModify = true;
                }
            }
            editBtn.setDisable(!canModify);
            deleteBtn.setDisable(!canModify);
        });

        createBtn.setOnAction(e -> showEventFormScene(null, events));
        editBtn.setOnAction(e -> {
            Event selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) showEventFormScene(selected, events);
        });
        deleteBtn.setOnAction(e -> {
            Event selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Видалити подію '" + selected.getName() + "'?", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            eventService.deleteEvent(selected.getId(), currentUser);
                            updateEventItems.run();
                            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Подію видалено.");
                        } catch (SecurityException secEx) {
                            showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося видалити подію: " + ex.getMessage());
                        }
                    }
                });
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateEventItems.run());

        HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
        crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(10);
        controlsGrid.add(searchField, 0,0);
        controlsGrid.add(crudButtonsBox, 1,0);
        ColumnConstraints colSearch = new ColumnConstraints(); colSearch.setHgrow(Priority.ALWAYS);
        ColumnConstraints colButtons = new ColumnConstraints(); colButtons.setHgrow(Priority.SOMETIMES);
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
        ObservableList<Tag> tags = FXCollections.observableArrayList();

        Runnable updateTagItems = () -> {
            String currentSearchText = searchField.getText().toLowerCase();
            ObservableList<Tag> sourceList;
            try {
                sourceList = FXCollections.observableArrayList(tagService.getAllTags());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося завантажити теги.");
                sourceList = FXCollections.observableArrayList();
            }
            if (currentSearchText.isEmpty()) {
                tags.setAll(sourceList);
            } else {
                tags.setAll(sourceList.stream()
                        .filter(tag -> tag.getName() != null && tag.getName().toLowerCase().contains(currentSearchText))
                        .collect(Collectors.toList()));
            }
            listView.setItems(tags);
        };
        updateTagItems.run();

        listView.setCellFactory(lv -> new ListCell<Tag>() {
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

        Button createBtn = new Button("Створити"); createBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button editBtn = new Button("Редагувати"); editBtn.setStyle(BUTTON_STYLE_SECONDARY);
        Button deleteBtn = new Button("Видалити"); deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);

        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disabled = newVal == null;
            editBtn.setDisable(disabled);
            deleteBtn.setDisable(disabled);
        });

        createBtn.setOnAction(e -> showTagFormScene(null, tags));
        editBtn.setOnAction(e -> {
            Tag selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) showTagFormScene(selected, tags);
        });
        deleteBtn.setOnAction(e -> {
            Tag selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Видалити тег '" + selected.getName() + "'? Це може вплинути на подорожі.", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            tagService.deleteTag(selected.getId());
                            updateTagItems.run();
                            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Тег видалено.");
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося видалити тег: " + ex.getMessage());
                        }
                    }
                });
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateTagItems.run());

        HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
        crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(10);
        controlsGrid.add(searchField, 0,0);
        controlsGrid.add(crudButtonsBox, 1,0);
        ColumnConstraints colSearch = new ColumnConstraints(); colSearch.setHgrow(Priority.ALWAYS);
        ColumnConstraints colButtons = new ColumnConstraints(); colButtons.setHgrow(Priority.SOMETIMES);
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
        ObservableList<Location> locations = FXCollections.observableArrayList();

        Runnable updateLocationItems = () -> {
            String currentSearchText = searchField.getText().toLowerCase();
            ObservableList<Location> sourceList;
            try {
                sourceList = FXCollections.observableArrayList(locationService.getAllLocations());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося завантажити локації.");
                sourceList = FXCollections.observableArrayList();
            }
            if (currentSearchText.isEmpty()) {
                locations.setAll(sourceList);
            } else {
                locations.setAll(sourceList.stream()
                        .filter(loc -> (loc.getName() != null && loc.getName().toLowerCase().contains(currentSearchText)) ||
                                (loc.getDescription() != null && loc.getDescription().toLowerCase().contains(currentSearchText)))
                        .collect(Collectors.toList()));
            }
            listView.setItems(locations);
        };
        updateLocationItems.run();

        listView.setCellFactory(lv -> new ListCell<Location>() {
            @Override
            protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle(LIST_CELL_STYLE_NORMAL);
                } else {
                    setText(item.getName() + (item.getDescription() != null && !item.getDescription().isEmpty() ? " (" + item.getDescription() + ")" : ""));
                    setTextFill(Color.WHITE);
                    if (isSelected()) {
                        setStyle(LIST_CELL_STYLE_SELECTED);
                    } else {
                        setStyle(LIST_CELL_STYLE_NORMAL);
                    }
                }
            }
        });

        Button createBtn = new Button("Створити"); createBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button editBtn = new Button("Редагувати"); editBtn.setStyle(BUTTON_STYLE_SECONDARY);
        Button deleteBtn = new Button("Видалити"); deleteBtn.setStyle(BUTTON_STYLE_SECONDARY);

        editBtn.setDisable(true);
        deleteBtn.setDisable(true);

        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean disabled = newVal == null;
            editBtn.setDisable(disabled);
            deleteBtn.setDisable(disabled);
        });

        createBtn.setOnAction(e -> showLocationFormScene(null, locations));
        editBtn.setOnAction(e -> {
            Location selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) showLocationFormScene(selected, locations);
        });
        deleteBtn.setOnAction(e -> {
            Location selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Видалити локацію '" + selected.getName() + "'? Це може вплинути на подорожі та події.", ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        try {
                            locationService.deleteLocation(selected.getId());
                            updateLocationItems.run();
                            showAlert(Alert.AlertType.INFORMATION, "Успіх", "Локацію видалено.");
                        } catch (Exception ex) {
                            showAlert(Alert.AlertType.ERROR, "Помилка", "Не вдалося видалити локацію: " + ex.getMessage());
                        }
                    }
                });
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> updateLocationItems.run());

        HBox crudButtonsBox = new HBox(10, createBtn, editBtn, deleteBtn);
        crudButtonsBox.setAlignment(Pos.CENTER_RIGHT);

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(10);
        controlsGrid.add(searchField, 0,0);
        controlsGrid.add(crudButtonsBox, 1,0);
        ColumnConstraints colSearch = new ColumnConstraints(); colSearch.setHgrow(Priority.ALWAYS);
        ColumnConstraints colButtons = new ColumnConstraints(); colButtons.setHgrow(Priority.SOMETIMES);
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

        HBox journeyInputBox = new HBox(10, new Label("ID Подорожі:"), journeyIdField, loadPhotosBtn);
        journeyInputBox.setAlignment(Pos.CENTER_LEFT);
        ((Label)journeyInputBox.getChildren().get(0)).setStyle(LABEL_STYLE);


        TilePane photoTilePane = new TilePane();
        photoTilePane.setPadding(new Insets(10));
        photoTilePane.setHgap(10);
        photoTilePane.setVgap(10);
        photoTilePane.setStyle("-fx-background-color: #2b2b2b;");

        ScrollPane scrollPane = new ScrollPane(photoTilePane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2b2b2b; -fx-background: #2b2b2b;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);


        Button uploadBtn = new Button("Завантажити нове фото");
        uploadBtn.setStyle(BUTTON_STYLE_PRIMARY);
        uploadBtn.setDisable(true);

        journeyIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            uploadBtn.setDisable(newVal.trim().isEmpty());
        });

        // Оголошуємо Runnable тут, щоб вона була доступна для всіх обробників
        final Runnable refreshPhotoTilePane = () -> {
            String journeyIdText = journeyIdField.getText();
            if (journeyIdText.isEmpty()) return;
            try {
                long journeyId = Long.parseLong(journeyIdText);
                photoTilePane.getChildren().clear();
                photoService.getPhotosByJourneyId(journeyId).forEach(photo -> {
                    try {
                        File imgFile = new File(photo.getFilePath());
                        if(imgFile.exists() && !imgFile.isDirectory()) {
                            Image image = new Image(new FileInputStream(photo.getFilePath()), 150, 150, true, true);
                            ImageView imageView = new ImageView(image);

                            Label descLabel = new Label(photo.getDescription() != null && !photo.getDescription().isEmpty() ? photo.getDescription() : "Без опису");
                            descLabel.setTextFill(Color.WHITE);
                            descLabel.setMaxWidth(140);
                            descLabel.setWrapText(true);
                            descLabel.setAlignment(Pos.CENTER);

                            Button deletePhotoButton = new Button("Видалити");
                            deletePhotoButton.setStyle(BUTTON_STYLE_DANGER);
                            deletePhotoButton.setOnAction(delEvent -> {
                                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Видалити цю фотографію?", ButtonType.YES, ButtonType.NO);
                                Optional<ButtonType> result = confirm.showAndWait();
                                if (result.isPresent() && result.get() == ButtonType.YES) {
                                    try {
                                        photoService.deletePhoto(photo.getId(), currentUser);
                                        refreshPhotoTilePane.run();
                                        showAlert(Alert.AlertType.INFORMATION, "Успіх", "Фотографію видалено.");
                                    } catch (SecurityException secEx) {
                                        showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                                    } catch (Exception ex) {
                                        showAlert(Alert.AlertType.ERROR, "Помилка видалення", ex.getMessage());
                                    }
                                }
                            });
                            Optional<Journey> journeyOpt = journeyService.getJourneyById(photo.getJourneyId());
                            deletePhotoButton.setDisable(!journeyOpt.map(j -> j.getUserId().equals(currentUser.getId())).orElse(true));

                            VBox photoBox = new VBox(5, imageView, descLabel, deletePhotoButton);
                            photoBox.setAlignment(Pos.CENTER);
                            photoBox.setPadding(new Insets(5));
                            photoBox.setStyle("-fx-border-color: #444; -fx-border-width: 1; -fx-background-color: #3c3f41; -fx-background-radius: 5; -fx-min-width: 160; -fx-max-width: 160;");
                            photoTilePane.getChildren().add(photoBox);
                        }
                    } catch (FileNotFoundException fnfEx) {
                        System.err.println("Файл фото не знайдено: " + photo.getFilePath());
                    } catch (Exception ex) {
                        System.err.println("Помилка завантаження зображення: " + photo.getFilePath() + " - " + ex.getMessage());
                    }
                });
            } catch (NumberFormatException ex) {
                // ID не число, можна нічого не робити або показати повідомлення
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Помилка завантаження", "Не вдалося завантажити фотографії.");
                ex.printStackTrace();
            }
        };


        loadPhotosBtn.setOnAction(e -> refreshPhotoTilePane.run());
        uploadBtn.setOnAction(e -> {
            String journeyIdText = journeyIdField.getText();
            if (journeyIdText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Увага", "Спочатку введіть ID подорожі, до якої завантажувати фото.");
                return;
            }
            try {
                long journeyId = Long.parseLong(journeyIdText);
                Optional<Journey> journeyOpt = journeyService.getJourneyById(journeyId);
                if(journeyOpt.isEmpty()){
                    showAlert(Alert.AlertType.ERROR, "Помилка", "Подорож з ID " + journeyId + " не знайдено. Неможливо завантажити фото.");
                    return;
                }
                if (!journeyOpt.get().getUserId().equals(currentUser.getId())) {
                    showAlert(Alert.AlertType.ERROR, "Помилка доступу", "Ви можете завантажувати фото тільки до своїх подорожей.");
                    return;
                }
                showPhotoUploadFormScene(journeyId, refreshPhotoTilePane);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Помилка", "ID подорожі має бути числом.");
            }
        });

        layout.getChildren().addAll(title, journeyInputBox, scrollPane, uploadBtn);
        return layout;
    }



    // --- Форми для створення/редагування ---
    private void showJourneyFormScene(Journey journeyToEdit) { /* ... (код як і раніше) ... */ }
    private void showEventFormScene(Event eventToEdit, ObservableList<Event> parentList) { /* ... (код як і раніше) ... */ }

    private void showLocationFormScene(Location locationToEdit, ObservableList<Location> parentList) {
        Stage formStage = new Stage();
        formStage.initModality(Modality.WINDOW_MODAL);
        formStage.initOwner(primaryStage); // Використовуємо primaryStage з MainApplicationFrame
        formStage.setTitle(locationToEdit == null ? "Створення нової локації" : "Редагування локації");

        GridPane grid = createGridPane();
        grid.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 20;");
        int rowIndex = 0;
        Label nameLabel = new Label("Назва:"); nameLabel.setStyle(LABEL_STYLE);
        grid.add(nameLabel, 0, rowIndex);
        TextField nameField = new TextField(locationToEdit != null ? locationToEdit.getName() : "");
        nameField.setStyle(INPUT_STYLE);
        grid.add(nameField, 1, rowIndex++);

        Label descLabel = new Label("Опис:"); descLabel.setStyle(LABEL_STYLE);
        grid.add(descLabel, 0, rowIndex);
        TextArea descriptionArea = new TextArea(locationToEdit != null && locationToEdit.getDescription() != null ? locationToEdit.getDescription() : "");
        descriptionArea.setStyle(INPUT_STYLE); descriptionArea.setWrapText(true); descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, rowIndex++);

        Button saveBtn = new Button("Зберегти"); saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button cancelBtn = new Button("Скасувати"); cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 1, rowIndex);

        saveBtn.setOnAction(e -> {
            String name = nameField.getText();
            String description = descriptionArea.getText();
            LocationCreationDto dto = new LocationCreationDto(name, description);
            Set<ConstraintViolation<LocationCreationDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Помилка валідації", violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n")));
                return;
            }
            try {
                if (locationToEdit == null) {
                    Location newLocation = locationService.createLocation(dto.getName(), dto.getDescription());
                    // parentList.add(newLocation); // Оновлення через refreshLocationList
                    showAlert(Alert.AlertType.INFORMATION, "Успіх", "Локацію '" + newLocation.getName() + "' створено.");
                } else {
                    Location updatedLocation = locationService.updateLocation(locationToEdit.getId(), dto.getName(), dto.getDescription());
                    // int index = parentList.indexOf(locationToEdit); // Оновлення через refreshLocationList
                    // if (index != -1) parentList.set(index, updatedLocation);
                    showAlert(Alert.AlertType.INFORMATION, "Успіх", "Локацію '" + updatedLocation.getName() + "' оновлено.");
                }
                formStage.close();
                refreshLocationList();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
            }
        });
        cancelBtn.setOnAction(e -> formStage.close());

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2b2b2b; -fx-background: #2b2b2b;");

        Scene scene = new Scene(scrollPane, 450, 300);
        formStage.setScene(scene);
        formStage.showAndWait();
    }

    private void showPhotoUploadFormScene(long journeyId, Runnable refreshCallback) { // Змінено другий параметр
        Stage formStage = new Stage();
        formStage.initModality(Modality.WINDOW_MODAL);
        formStage.initOwner(primaryStage);
        formStage.setTitle("Завантаження фото для подорожі ID: " + journeyId);

        GridPane grid = createGridPane();
        grid.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 20;");
        int rowIndex = 0;

        Label fileLabel = new Label("Файл фото:"); fileLabel.setStyle(LABEL_STYLE);
        grid.add(fileLabel, 0, rowIndex);
        TextField filePathField = new TextField();
        filePathField.setEditable(false);
        filePathField.setStyle(INPUT_STYLE);
        Button chooseFileBtn = new Button("Обрати файл..."); chooseFileBtn.setStyle(BUTTON_STYLE_SECONDARY);
        HBox fileBox = new HBox(5, filePathField, chooseFileBtn);
        grid.add(fileBox, 1, rowIndex++);

        final File[] selectedFile = new File[1];
        chooseFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Оберіть фото для завантаження");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Зображення", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                    new FileChooser.ExtensionFilter("Всі файли", "*.*")
            );
            File file = fileChooser.showOpenDialog(formStage);
            if (file != null) {
                filePathField.setText(file.getAbsolutePath());
                selectedFile[0] = file;
            }
        });

        Label descLabel = new Label("Опис:"); descLabel.setStyle(LABEL_STYLE);
        grid.add(descLabel, 0, rowIndex);
        TextArea descriptionArea = new TextArea();
        descriptionArea.setStyle(INPUT_STYLE); descriptionArea.setWrapText(true); descriptionArea.setPrefRowCount(3);
        grid.add(descriptionArea, 1, rowIndex++);

        Button saveBtn = new Button("Завантажити"); saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button cancelBtn = new Button("Скасувати"); cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 1, rowIndex);

        saveBtn.setOnAction(e -> {
            if (selectedFile[0] == null) {
                showAlert(Alert.AlertType.WARNING, "Файл не обрано", "Будь ласка, оберіть файл фотографії.");
                return;
            }
            String description = descriptionArea.getText();
            PhotoUploadDto dto = new PhotoUploadDto(journeyId, selectedFile[0].getAbsolutePath(), description);
            Set<ConstraintViolation<PhotoUploadDto>> violations = validator.validate(dto);
            if(!violations.isEmpty()){
                showAlert(Alert.AlertType.ERROR, "Помилка валідації", violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n")));
                return;
            }

            try {
                photoService.uploadPhoto(dto, currentUser);
                showAlert(Alert.AlertType.INFORMATION, "Успіх", "Фотографію завантажено.");
                if (refreshCallback != null) {
                    refreshCallback.run(); // Викликаємо оновлення TilePane
                }
                formStage.close();
            } catch (SecurityException secEx){
                showAlert(Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
            }catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Помилка завантаження", ex.getMessage());
                ex.printStackTrace();
            }
        });
        cancelBtn.setOnAction(e -> formStage.close());

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2b2b2b; -fx-background: #2b2b2b;");

        Scene scene = new Scene(scrollPane, 500, 300);
        formStage.setScene(scene);
        formStage.showAndWait();
    }

    private void showTagFormScene(Tag tagToEdit, ObservableList<Tag> parentList) {
        Stage formStage = new Stage();
        formStage.initModality(Modality.WINDOW_MODAL);
        formStage.initOwner(primaryStage);
        formStage.setTitle(tagToEdit == null ? "Створення нового тегу" : "Редагування тегу");

        GridPane grid = createGridPane();
        grid.setStyle("-fx-background-color: #2b2b2b; -fx-padding: 20;");
        int rowIndex = 0;

        Label nameLabel = new Label("Назва тегу:"); nameLabel.setStyle(LABEL_STYLE);
        grid.add(nameLabel, 0, rowIndex);
        TextField nameField = new TextField(tagToEdit != null ? tagToEdit.getName() : "");
        nameField.setStyle(INPUT_STYLE);
        grid.add(nameField, 1, rowIndex++);

        Button saveBtn = new Button("Зберегти"); saveBtn.setStyle(BUTTON_STYLE_PRIMARY);
        Button cancelBtn = new Button("Скасувати"); cancelBtn.setStyle(BUTTON_STYLE_SECONDARY);
        HBox buttons = new HBox(10, cancelBtn, saveBtn);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttons, 1, rowIndex);

        saveBtn.setOnAction(e -> {
            String name = nameField.getText();
            TagCreationDto dto = new TagCreationDto(name);
            Set<ConstraintViolation<TagCreationDto>> violations = validator.validate(dto);
            if (!violations.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Помилка валідації", violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining("\n")));
                return;
            }
            try {
                if (tagToEdit == null) {
                    Tag newTag = tagService.createTag(dto.getName());
                    // parentList.add(newTag); // Оновлення через refreshTagList
                    showAlert(Alert.AlertType.INFORMATION, "Успіх", "Тег '" + newTag.getName() + "' створено.");
                } else {
                    Tag updatedTag = tagService.updateTag(tagToEdit.getId(), dto.getName());
                    // int index = parentList.indexOf(tagToEdit); // Оновлення через refreshTagList
                    // if (index != -1) parentList.set(index, updatedTag);
                    showAlert(Alert.AlertType.INFORMATION, "Успіх", "Тег '" + updatedTag.getName() + "' оновлено.");
                }
                formStage.close();
                refreshTagList();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Помилка збереження", ex.getMessage());
            }
        });
        cancelBtn.setOnAction(e -> formStage.close());

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #2b2b2b; -fx-background: #2b2b2b;");

        Scene scene = new Scene(scrollPane, 400, 200);
        formStage.setScene(scene);
        formStage.showAndWait();
    }

    // --- Методи оновлення списків ---
    private void refreshJourneyList() { /* ... (код як і раніше) ... */ }
    private void refreshEventList() { /* ... (код як і раніше) ... */ }

    private void refreshTagList() {
        if (mainLayout != null && mainLayout.getCenter() != null) {
            Node centerNode = mainLayout.getCenter();
            if (centerNode instanceof VBox) {
                VBox tagPaneLayout = (VBox) centerNode;
                Optional<Node> listViewOpt = tagPaneLayout.getChildren().stream()
                        .filter(n -> n instanceof ListView && "tagListView".equals(n.getId()))
                        .findFirst();
                if (listViewOpt.isPresent()) {
                    @SuppressWarnings("unchecked")
                    ListView<Tag> listView = (ListView<Tag>) listViewOpt.get();
                    try {
                        listView.getItems().setAll(tagService.getAllTags());
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Помилка оновлення", "Не вдалося оновити список тегів.");
                    }
                }
            }
        }
    }

    private void refreshLocationList() {
        if (mainLayout != null && mainLayout.getCenter() != null) {
            Node centerNode = mainLayout.getCenter();
            if (centerNode instanceof VBox) {
                VBox locationPaneLayout = (VBox) centerNode;
                Optional<Node> listViewOpt = locationPaneLayout.getChildren().stream()
                        .filter(n -> n instanceof ListView && "locationListView".equals(n.getId()))
                        .findFirst();
                if (listViewOpt.isPresent()) {
                    @SuppressWarnings("unchecked")
                    ListView<Location> listView = (ListView<Location>) listViewOpt.get();
                    try {
                        listView.getItems().setAll(locationService.getAllLocations());
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.ERROR, "Помилка оновлення", "Не вдалося оновити список локацій.");
                    }
                }
            }
        }
    }

    // --- Допоміжні методи ---
    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.TOP_LEFT); // Вирівнювання для форм
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20)); // Стандартні відступи для форм
        return grid;
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Не показувати додатковий заголовок у тілі повідомлення
        alert.setContentText(message);

        // Застосування стилю до діалогового вікна Alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(new PrimerDark().getUserAgentStylesheet()); // Застосовуємо тему AtlantaFX
        dialogPane.setStyle("-fx-background-color: #2b2b2b;"); // Фон для діалогового вікна

        // Стилізація тексту контенту
        Node content = dialogPane.lookup(".content.label");
        if (content instanceof Label) {
            ((Label) content).setTextFill(Color.WHITE); // Білий текст на темному фоні
        }

        // Стилізація кнопок в Alert
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            if (button != null) {
                // Можна задати загальний стиль для кнопок або розрізняти за типом
                button.setStyle(BUTTON_STYLE_SECONDARY); // Використовуємо ваш стиль для вторинних кнопок
            }
        });

        alert.showAndWait();
    }

    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.WARNING, "Некоректна дата", "Формат дати має бути дд.мм.рррр. Поле буде проігноровано.");
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
            showAlert(Alert.AlertType.WARNING, "Некоректний час", "Формат часу має бути гг:хх (наприклад, 14:30). Поле буде проігноровано.");
            return null;
        }
    }
}
