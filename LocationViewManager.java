package com.trailtales.ui.managers;

import com.trailtales.entity.Location;
import com.trailtales.entity.User;
import com.trailtales.service.LocationService;
import com.trailtales.ui.MainApplicationFrame;
import com.trailtales.ui.UIConstants;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LocationViewManager {

  private final LocationService locationService;
  private final User currentUser;
  private final MainApplicationFrame mainAppFrame;

  private ListView<Location> locationListView;
  private TextField searchField;

  public LocationViewManager(
      LocationService locationService, User currentUser, MainApplicationFrame mainAppFrame) {
    this.locationService = locationService;
    this.currentUser = currentUser;
    this.mainAppFrame = mainAppFrame;
  }

  public Pane createLocationPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle(UIConstants.BACKGROUND_STYLE_MAIN_APP);

    Text title = new Text("Список Локацій");
    title.setStyle(UIConstants.TITLE_STYLE);
    layout.getChildren().add(title);

    searchField = new TextField();
    searchField.setPromptText("Пошук локацій...");
    searchField.setStyle(UIConstants.INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    locationListView = new ListView<>();
    locationListView.setId("locationListView");
    locationListView.setStyle(UIConstants.LIST_VIEW_STYLE);
    locationListView.setCellFactory(
        lv ->
            new ListCell<Location>() {
              @Override
              protected void updateItem(Location item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                } else {
                  setText(buildLocationDisplayText(item));
                  setTextFill(Color.WHITE);
                  setStyle(
                      isSelected()
                          ? UIConstants.LIST_CELL_STYLE_SELECTED
                          : UIConstants.LIST_CELL_STYLE_NORMAL);
                }
              }
            });

    refreshLocationList(searchField.getText());
    searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshLocationList(newVal));

    Button createBtn = new Button("Створити");
    createBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    locationListView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> updateButtonStates(newVal, editBtn, deleteBtn));

    createBtn.setOnAction(e -> mainAppFrame.showLocationFormScene(null, this));
    editBtn.setOnAction(
        e -> {
          Location selected = locationListView.getSelectionModel().getSelectedItem();
          if (selected != null) mainAppFrame.showLocationFormScene(selected, this);
        });
    deleteBtn.setOnAction(e -> handleDeleteAction());

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

    layout.getChildren().addAll(controlsGrid, locationListView);
    VBox.setVgrow(locationListView, Priority.ALWAYS);
    return layout;
  }

  public void refreshLocationList(String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Location> sourceList = loadLocations();
    if (currentSearchText.isEmpty()) {
      locationListView.setItems(sourceList);
    } else {
      locationListView.setItems(
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

  private String buildLocationDisplayText(Location item) {
    String description =
        (item.getDescription() != null && !item.getDescription().isEmpty())
            ? " (" + item.getDescription() + ")"
            : "";
    return item.getName() + description;
  }

  private ObservableList<Location> loadLocations() {
    try {
      return FXCollections.observableArrayList(locationService.getAllLocations());
    } catch (Exception e) {
      mainAppFrame.showAlert(
          Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список локацій: " + e.getMessage());
      e.printStackTrace();
      return FXCollections.observableArrayList();
    }
  }

  private void updateButtonStates(Location selected, Button editBtn, Button deleteBtn) {
    boolean disabled = selected == null;
    editBtn.setDisable(disabled);
    deleteBtn.setDisable(disabled);
  }

  private void handleDeleteAction() {
    Location selected = locationListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      Alert confirm =
          new Alert(
              Alert.AlertType.CONFIRMATION,
              "Видалити локацію '"
                  + selected.getName()
                  + "'? Це може вплинути на подорожі та події.",
              ButtonType.YES,
              ButtonType.NO);
      mainAppFrame.applyDialogStyles(confirm.getDialogPane());
      confirm
          .showAndWait()
          .ifPresent(
              response -> {
                if (response == ButtonType.YES) {
                  try {
                    locationService.deleteLocation(selected.getId());
                    refreshLocationList(searchField.getText());
                    mainAppFrame.showAlert(
                        Alert.AlertType.INFORMATION, "Успіх", "Локацію видалено.");
                  } catch (Exception ex) {
                    mainAppFrame.showAlert(
                        Alert.AlertType.ERROR,
                        "Помилка",
                        "Не вдалося видалити локацію: " + ex.getMessage());
                  }
                }
              });
    }
  }

  public String getCurrentSearchText() {
    return searchField != null ? searchField.getText() : "";
  }
}
