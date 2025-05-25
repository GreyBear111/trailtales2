package com.trailtales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagCreationDto {

  @NotBlank(message = "Назва тегу не може бути порожньою")
  @Size(min = 2, max = 50, message = "Назва тегу повинна бути від 2 до 50 символів")
  private String name;
}
