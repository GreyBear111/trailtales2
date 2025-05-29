package com.trailtales.ui.managers;

import static com.trailtales.ui.MainApplicationFrame.DATE_FORMATTER;

import com.trailtales.entity.Event;
import com.trailtales.entity.Journey;
import com.trailtales.entity.User;
import com.trailtales.service.EventService;
import com.trailtales.service.JourneyService;
import com.trailtales.ui.MainApplicationFrame;
import com.trailtales.ui.UIConstants;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class EventViewManager {

  private final EventService eventService;
  private final JourneyService journeyService;
  private final User currentUser;
  private final MainApplicationFrame mainAppFrame;

  private ListView<Event> eventListView;
  private TextField searchField;

  public EventViewManager(
      EventService eventService,
      JourneyService journeyService,
      User currentUser,
      MainApplicationFrame mainAppFrame) {
    this.eventService = eventService;
    this.journeyService = journeyService;
    this.currentUser = currentUser;
    this.mainAppFrame = mainAppFrame;
  }

  public Pane createEventPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle(UIConstants.BACKGROUND_STYLE_MAIN_APP);

    Text title = new Text("Список Подій");
    title.setStyle(UIConstants.TITLE_STYLE);
    layout.getChildren().add(title);

    searchField = new TextField();
    searchField.setPromptText("Пошук подій...");
    searchField.setStyle(UIConstants.INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    eventListView = new ListView<>();
    eventListView.setId("eventListView");
    eventListView.setStyle(UIConstants.LIST_VIEW_STYLE);
    eventListView.setCellFactory(
        lv ->
            new ListCell<Event>() {
              @Override
              protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                } else {
                  String journeyNameStr = getJourneyName(item);
                  setText(
                      item.getName()
                          + (item.getEventDate() != null
                              ? " [" + DATE_FORMATTER.format(item.getEventDate()) + "]"
                              : "")
                          + journeyNameStr);
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(UIConstants.LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });

    refreshEventList(searchField.getText());
    searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshEventList(newVal));

    Button createBtn = new Button("Створити");
    createBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    eventListView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              boolean canModify = canModifyEvent(newVal);
              editBtn.setDisable(!canModify);
              deleteBtn.setDisable(!canModify);
            });

    createBtn.setOnAction(e -> mainAppFrame.showEventFormScene(null, this));
    editBtn.setOnAction(
        e -> {
          Event selected = eventListView.getSelectionModel().getSelectedItem();
          if (selected != null) mainAppFrame.showEventFormScene(selected, this);
        });
    deleteBtn.setOnAction(
        e -> {
          Event selected = eventListView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            Alert confirm =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Видалити подію '" + selected.getName() + "'?",
                    ButtonType.YES,
                    ButtonType.NO);
            mainAppFrame.applyDialogStyles(confirm.getDialogPane());
            confirm
                .showAndWait()
                .ifPresent(
                    response -> {
                      if (response == ButtonType.YES) {
                        try {
                          eventService.deleteEvent(selected.getId(), currentUser);
                          refreshEventList(searchField.getText());
                          mainAppFrame.showAlert(
                              Alert.AlertType.INFORMATION, "Успіх", "Подію видалено.");
                        } catch (SecurityException secEx) {
                          mainAppFrame.showAlert(
                              Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                        } catch (Exception ex) {
                          mainAppFrame.showAlert(
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

    layout.getChildren().addAll(controlsGrid, eventListView);
    VBox.setVgrow(eventListView, Priority.ALWAYS);
    return layout;
  }

  public void refreshEventList(String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Event> sourceList = loadEvents();
    if (currentSearchText.isEmpty()) {
      eventListView.setItems(sourceList);
    } else {
      eventListView.setItems(
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

  private ObservableList<Event> loadEvents() {
    try {
      return FXCollections.observableArrayList(eventService.getAllEvents());
    } catch (Exception e) {
      mainAppFrame.showAlert(
          Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список подій: " + e.getMessage());
      e.printStackTrace();
      return FXCollections.observableArrayList();
    }
  }

  private String getJourneyName(Event item) {
    if (item.getJourneyId() == null) {
      return "";
    }
    Optional<Journey> journeyOpt = journeyService.getJourneyById(item.getJourneyId());
    return journeyOpt.map(j -> " (Подорож: " + j.getName() + ")").orElse("");
  }

  private boolean canModifyEvent(Event event) {
    if (event == null || currentUser == null) {
      return false;
    }
    if (event.getJourneyId() != null) {
      Optional<Journey> journeyOpt = journeyService.getJourneyById(event.getJourneyId());
      return journeyOpt.map(j -> j.getUserId().equals(currentUser.getId())).orElse(false);
    }
    return true;
  }

  public String getCurrentSearchText() {
    return searchField != null ? searchField.getText() : "";
  }
}
