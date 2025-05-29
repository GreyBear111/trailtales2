package com.trailtales.ui.managers;

import com.trailtales.entity.Journey;
import com.trailtales.entity.User;
import com.trailtales.service.JourneyService;
import com.trailtales.service.PhotoService;
import com.trailtales.ui.MainApplicationFrame;
import com.trailtales.ui.UIConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class PhotoViewManager {

  private final PhotoService photoService;
  private final JourneyService journeyService;
  private final User currentUser;
  private final MainApplicationFrame mainAppFrame;

  private TilePane photoTilePane;
  private TextField journeyIdField;

  public PhotoViewManager(
      PhotoService photoService,
      JourneyService journeyService,
      User currentUser,
      MainApplicationFrame mainAppFrame) {
    this.photoService = photoService;
    this.journeyService = journeyService;
    this.currentUser = currentUser;
    this.mainAppFrame = mainAppFrame;
  }

  public Pane createPhotoPane() {
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.setStyle(UIConstants.BACKGROUND_STYLE_MAIN_APP);

    Text title = new Text("Галерея Фотографій");
    title.setStyle(UIConstants.TITLE_STYLE);

    journeyIdField = new TextField();
    journeyIdField.setPromptText("Введіть ID подорожі для фото");
    journeyIdField.setStyle(UIConstants.INPUT_STYLE);

    Button loadPhotosBtn = new Button("Показати фото");
    loadPhotosBtn.setStyle(UIConstants.BUTTON_STYLE_SECONDARY);

    Label journeyIdLabel = new Label("ID Подорожі:");
    journeyIdLabel.setStyle(UIConstants.LABEL_STYLE);
    HBox journeyInputBox = new HBox(10, journeyIdLabel, journeyIdField, loadPhotosBtn);
    journeyInputBox.setAlignment(Pos.CENTER_LEFT);

    photoTilePane = new TilePane();
    photoTilePane.setPadding(new Insets(10));
    photoTilePane.setHgap(10);
    photoTilePane.setVgap(10);
    photoTilePane.setStyle(UIConstants.TILE_PANE_STYLE);

    ScrollPane scrollPane = new ScrollPane(photoTilePane);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(true);
    scrollPane.setStyle(UIConstants.SCROLL_PANE_STYLE);
    VBox.setVgrow(scrollPane, Priority.ALWAYS);

    Button uploadBtn = new Button("Завантажити нове фото");
    uploadBtn.setStyle(UIConstants.BUTTON_STYLE_PRIMARY);
    uploadBtn.setDisable(true); // Initially disabled

    journeyIdField
        .textProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              uploadBtn.setDisable(newVal == null || newVal.trim().isEmpty());
            });

    loadPhotosBtn.setOnAction(e -> refreshPhotoTilePane());
    uploadBtn.setOnAction(
        e -> {
          String journeyIdText = journeyIdField.getText();
          try {
            long journeyId = Long.parseLong(journeyIdText);
            Optional<Journey> journeyOpt = journeyService.getJourneyById(journeyId);
            if (journeyOpt.isEmpty()) {
              mainAppFrame.showAlert(
                  Alert.AlertType.ERROR, "Помилка", "Подорож з ID " + journeyId + " не знайдено.");
              return;
            }
            if (!journeyOpt.get().getUserId().equals(currentUser.getId())) {
              mainAppFrame.showAlert(
                  Alert.AlertType.ERROR,
                  "Помилка доступу",
                  "Ви можете завантажувати фото тільки до своїх подорожей.");
              return;
            }
            mainAppFrame.showPhotoUploadFormScene(journeyId, this);
          } catch (NumberFormatException ex) {
            mainAppFrame.showAlert(
                Alert.AlertType.ERROR, "Помилка", "ID подорожі має бути числом.");
          }
        });

    layout.getChildren().addAll(title, journeyInputBox, scrollPane, uploadBtn);
    return layout;
  }

  public void refreshPhotoTilePane() {
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
                    deletePhotoButton.setStyle(UIConstants.BUTTON_STYLE_DANGER);
                    deletePhotoButton.setOnAction(
                        delEvent -> {
                          Alert confirm =
                              new Alert(
                                  Alert.AlertType.CONFIRMATION,
                                  "Видалити цю фотографію?",
                                  ButtonType.YES,
                                  ButtonType.NO);
                          mainAppFrame.applyDialogStyles(confirm.getDialogPane());
                          Optional<ButtonType> result = confirm.showAndWait();
                          if (result.isPresent() && result.get() == ButtonType.YES) {
                            try {
                              photoService.deletePhoto(photo.getId(), currentUser);
                              refreshPhotoTilePane(); // Refresh after delete
                              mainAppFrame.showAlert(
                                  Alert.AlertType.INFORMATION, "Успіх", "Фотографію видалено.");
                            } catch (SecurityException secEx) {
                              mainAppFrame.showAlert(
                                  Alert.AlertType.ERROR, "Помилка доступу", secEx.getMessage());
                            } catch (Exception ex) {
                              mainAppFrame.showAlert(
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
                    photoBox.setStyle(UIConstants.PHOTO_BOX_STYLE);
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
      mainAppFrame.showAlert(
          Alert.AlertType.ERROR, "Помилка", "ID подорожі має бути числовим значенням.");
    } catch (Exception ex) {
      mainAppFrame.showAlert(
          Alert.AlertType.ERROR,
          "Помилка завантаження",
          "Не вдалося завантажити фотографії: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  public String getCurrentJourneyIdText() {
    return journeyIdField != null ? journeyIdField.getText() : "";
  }
}
