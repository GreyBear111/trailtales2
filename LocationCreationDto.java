package com.trailtales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationCreationDto {

  @NotBlank(message = "Назва локації не може бути порожньою")
  @Size(min = 2, max = 100, message = "Назва локації повинна бути від 2 до 100 символів")
  private String name;

  @Size(max = 255, message = "Опис локації не може перевищувати 255 символів")
  private String description; // Необов'язковий опис локації
}
