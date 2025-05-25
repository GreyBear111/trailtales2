package com.trailtales.service.impl;

import com.trailtales.dto.PhotoUploadDto;
import com.trailtales.entity.Journey;
import com.trailtales.entity.Photo;
import com.trailtales.entity.User;
import com.trailtales.repository.JourneyRepository;
import com.trailtales.repository.PhotoRepository;
import com.trailtales.service.PhotoService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PhotoServiceImpl implements PhotoService {

  private final PhotoRepository photoRepository;
  private final JourneyRepository journeyRepository;
  private final String uploadDir;
  private final Validator validator;

  @Autowired
  public PhotoServiceImpl(
      PhotoRepository photoRepository,
      JourneyRepository journeyRepository,
      @Value("${trailtales.upload.dir}") String uploadDir,
      Validator validator) {
    this.photoRepository = photoRepository;
    this.journeyRepository = journeyRepository;
    this.uploadDir = uploadDir;
    this.validator = validator;
    try {
      Files.createDirectories(Paths.get(this.uploadDir));
    } catch (IOException e) {
      throw new RuntimeException(
          "Не вдалося створити директорію для завантажень: " + this.uploadDir, e);
    }
  }

  @Override
  @Transactional
  public Photo uploadPhoto(PhotoUploadDto uploadDto, User currentUser) {
    Set<ConstraintViolation<PhotoUploadDto>> violations = validator.validate(uploadDto);
    if (!violations.isEmpty()) {
      String errors =
          violations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.joining("; "));
      throw new IllegalArgumentException("Помилки валідації при завантаженні фото: " + errors);
    }

    Journey journey =
        journeyRepository
            .findById(uploadDto.getJourneyId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати JourneyNotFoundException
                        "Подорож з ID " + uploadDto.getJourneyId() + " не знайдено."));

    if (!journey.getUserId().equals(currentUser.getId())) {
      throw new SecurityException(
          "Ви не маєте дозволу на завантаження фотографій до цієї подорожі.");
    }

    Path sourcePath = Paths.get(uploadDto.getSourceFilePath());
    String originalFileName = sourcePath.getFileName().toString();
    String fileName = originalFileName;
    Path destinationPath = Paths.get(uploadDir, fileName);
    int count = 0;

    while (Files.exists(destinationPath)) {
      count++;
      String baseName = "";
      String extension = "";
      int dotIndex = originalFileName.lastIndexOf('.');
      if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
        baseName = originalFileName.substring(0, dotIndex);
        extension = "." + originalFileName.substring(dotIndex + 1);
      } else {
        baseName = originalFileName;
      }
      fileName = baseName + "_" + count + extension;
      destinationPath = Paths.get(uploadDir, fileName);
    }

    try {
      Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Не вдалося скопіювати файл фотографії: " + e.getMessage(), e);
    }

    Photo photo = new Photo();
    photo.setJourneyId(uploadDto.getJourneyId());
    photo.setUserId(currentUser.getId());
    photo.setFilePath(destinationPath.toString());
    photo.setDescription(uploadDto.getDescription());
    photo.setCreatedAt(LocalDateTime.now());
    photo.setUpdatedAt(LocalDateTime.now());

    return photoRepository.save(photo);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Photo> getPhotoById(Long id) {
    return photoRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Photo> getPhotosByJourneyId(Long journeyId) {
    return photoRepository.findByJourneyId(journeyId);
  }

  @Override
  @Transactional
  public Photo updatePhotoDescription(Long photoId, String newDescription, User currentUser) {
    Photo photo =
        photoRepository
            .findById(photoId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати PhotoNotFoundException
                        "Фотографія з ID " + photoId + " не знайдено."));

    Journey journey =
        journeyRepository
            .findById(photo.getJourneyId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати JourneyNotFoundException
                        "Пов'язана подорож для фотографії ID "
                            + photo.getJourneyId()
                            + " не знайдена."));

    boolean isOwnerOfPhoto = photo.getUserId().equals(currentUser.getId());
    boolean isOwnerOfJourney = journey.getUserId().equals(currentUser.getId());

    if (!isOwnerOfPhoto && !isOwnerOfJourney) {
      throw new SecurityException("Ви не маєте дозволу на оновлення цієї фотографії.");
    }

    photo.setDescription(newDescription);
    photo.setUpdatedAt(LocalDateTime.now());
    return photoRepository.save(photo);
  }

  @Override
  @Transactional
  public void deletePhoto(Long id, User currentUser) {
    Photo photoToDelete =
        photoRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати PhotoNotFoundException
                        "Фотографія з ID " + id + " не знайдена для видалення."));

    Journey journey =
        journeyRepository
            .findById(photoToDelete.getJourneyId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException( // Можна використовувати JourneyNotFoundException
                        "Пов'язана подорож для фотографії ID "
                            + photoToDelete.getJourneyId()
                            + " не знайдена."));

    boolean isOwnerOfPhoto = photoToDelete.getUserId().equals(currentUser.getId());
    boolean isOwnerOfJourney = journey.getUserId().equals(currentUser.getId());

    if (!isOwnerOfPhoto && !isOwnerOfJourney) {
      throw new SecurityException("Ви не маєте дозволу на видалення цієї фотографії.");
    }

    try {
      Files.deleteIfExists(Paths.get(photoToDelete.getFilePath()));
    } catch (IOException e) {
      System.err.println(
          "Помилка при видаленні файлу фотографії: "
              + photoToDelete.getFilePath()
              + " - "
              + e.getMessage());
    }

    photoRepository.deleteById(id);
  }
}
