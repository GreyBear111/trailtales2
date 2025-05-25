package com.trailtales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadDto {

  @NotNull(message = "ID подорожі не може бути порожнім")
  private Long journeyId;

  @NotBlank(message = "Шлях до файлу фотографії не може бути порожнім")
  private String sourceFilePath; // Шлях до файлу на локальній системі користувача

  @Size(max = 500, message = "Опис фотографії не може перевищувати 500 символів")
  private String description;
}
