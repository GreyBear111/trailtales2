package com.trailtales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCreationDto {

  @NotBlank(message = "Назва події не може бути порожньою")
  @Size(min = 3, max = 100, message = "Назва події повинна бути від 3 до 100 символів")
  private String name;

  @Size(max = 500, message = "Опис події не може перевищувати 500 символів")
  private String description;

  private LocalDate eventDate;
  private LocalTime eventTime;

  private Long journeyId; // ID подорожі, до якої належить подія (може бути null)

  @Size(max = 100, message = "Назва локації події не може перевищувати 100 символів")
  private String locationName; // Назва локації події (рядок)

  @Size(max = 255, message = "Опис локації події не може перевищувати 255 символів")
  private String locationDescription; // ДОДАНО: Опис локації
}
