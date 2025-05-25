package com.trailtales.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
  private Long id;
  private Long journeyId; // ID подорожі, до якої належить фото
  private Long userId; // ID користувача, який завантажив фото (для дозволів)
  private String filePath; // Шлях до файлу на сервері (або в локальній файловій системі)
  private String description; // Опис фотографії (необов'язково)
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
