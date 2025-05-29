package com.trailtales.ui.managers;

import com.trailtales.entity.Tag;
import com.trailtales.entity.User;
import com.trailtales.service.TagService;
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

public class TagViewManager {

  private final TagService tagService;
  private final User currentUser;
  private final MainApplicationFrame mainAppFrame;

  private ListView<Tag> tagListView;
  private TextField searchField;

  public TagViewManager(
      TagService tagService, User currentUser, MainApplicationFrame mainAppFrame) {
    this.tagService = tagService;
    this.currentUser = currentUser;
    this.mainAppFrame = mainAppFrame;
  }

  public Pane createTagPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle(UIConstants.BACKGROUND_STYLE_MAIN_APP);

    Text title = new Text("Список Тегів");
    title.setStyle(UIConstants.TITLE_STYLE);
    layout.getChildren().add(title);

    searchField = new TextField();
    searchField.setPromptText("Пошук тегів...");
    searchField.setStyle(UIConstants.INPUT_STYLE);
    searchField.setMaxWidth(Double.MAX_VALUE);

    tagListView = new ListView<>();
    tagListView.setId("tagListView");
    tagListView.setStyle(UIConstants.LIST_VIEW_STYLE);
    tagListView.setCellFactory(
        lv ->
            new ListCell<Tag>() {
              @Override
              protected void updateItem(Tag item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                  setText(null);
                  setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                } else {
                  setText(item.getName());
                  setTextFill(Color.WHITE);
                  if (isSelected()) {
                    setStyle(UIConstants.LIST_CELL_STYLE_SELECTED);
                  } else {
                    setStyle(UIConstants.LIST_CELL_STYLE_NORMAL);
                  }
                }
              }
            });

    refreshTagList(searchField.getText());
    searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTagList(newVal));

    Button createBtn = new Button("Створити");
    createBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    Button editBtn = new Button("Редагувати");
    editBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);
    Button deleteBtn = new Button("Видалити");
    deleteBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);

    editBtn.setDisable(true);
    deleteBtn.setDisable(true);

    tagListView
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              boolean disabled = newVal == null;
              editBtn.setDisable(disabled);
              deleteBtn.setDisable(disabled);
            });

    createBtn.setOnAction(e -> mainAppFrame.showTagFormScene(null, this));
    editBtn.setOnAction(
        e -> {
          Tag selected = tagListView.getSelectionModel().getSelectedItem();
          if (selected != null) mainAppFrame.showTagFormScene(selected, this);
        });
    deleteBtn.setOnAction(
        e -> {
          Tag selected = tagListView.getSelectionModel().getSelectedItem();
          if (selected != null) {
            Alert confirm =
                new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Видалити тег '" + selected.getName() + "'? Це може вплинути на подорожі.",
                    ButtonType.YES,
                    ButtonType.NO);
            mainAppFrame.applyDialogStyles(confirm.getDialogPane());
            confirm
                .showAndWait()
                .ifPresent(
                    response -> {
                      if (response == ButtonType.YES) {
                        try {
                          tagService.deleteTag(selected.getId());
                          refreshTagList(searchField.getText());
                          mainAppFrame.showAlert(
                              Alert.AlertType.INFORMATION, "Успіх", "Тег видалено.");
                        } catch (Exception ex) {
                          mainAppFrame.showAlert(
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

    layout.getChildren().addAll(controlsGrid, tagListView);
    VBox.setVgrow(tagListView, Priority.ALWAYS);
    return layout;
  }

  public void refreshTagList(String searchText) {
    String currentSearchText = searchText != null ? searchText.toLowerCase() : "";
    ObservableList<Tag> sourceList;
    try {
      sourceList = FXCollections.observableArrayList(tagService.getAllTags());
    } catch (Exception e) {
      mainAppFrame.showAlert(
          Alert.AlertType.ERROR, "Помилка", "Не вдалося оновити список тегів: " + e.getMessage());
      sourceList = FXCollections.observableArrayList();
      e.printStackTrace();
    }

    if (currentSearchText.isEmpty()) {
      tagListView.setItems(sourceList);
    } else {
      tagListView.setItems(
          sourceList.stream()
              .filter(
                  tag ->
                      tag.getName() != null
                          && tag.getName().toLowerCase().contains(currentSearchText))
              .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }
  }

  public String getCurrentSearchText() {
    return searchField != null ? searchField.getText() : "";
  }
}
