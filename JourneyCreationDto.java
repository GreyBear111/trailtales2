package com.trailtales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set; // Імпорт для Set
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JourneyCreationDto {

  @NotBlank(message = "Назва подорожі не може бути порожньою")
  @Size(min = 3, max = 100, message = "Назва подорожі повинна бути від 3 до 100 символів")
  private String name;

  @Size(max = 500, message = "Опис подорожі не може перевищувати 500 символів")
  private String description;

  private LocalDate startDate;
  private LocalDate endDate;

  private Set<String> tagNames; // Змінено назву та тип

  @Size(max = 100, message = "Назва початкової локації не може перевищувати 100 символів")
  private String originLocationName;

  @Size(max = 255, message = "Опис початкової локації не може перевищувати 255 символів")
  private String originLocationDescription; // ДОДАНО: Опис початкової локації

  @Size(max = 100, message = "Назва кінцевої локації не може перевищувати 100 символів")
  private String destinationLocationName;

  @Size(max = 255, message = "Опис кінцевої локації не може перевищувати 255 символів")
  private String destinationLocationDescription; // ДОДАНО: Опис кінцевої локації
}
