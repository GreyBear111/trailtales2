package com.trailtales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginDto {

  @NotBlank(message = "Ім'я користувача або Email не може бути порожнім")
  @Size(
      min = 3,
      max = 100,
      message = "Ім'я користувача або Email повинно бути від 3 до 100 символів")
  private String identifier; // Може бути username або email

  @NotBlank(message = "Пароль не може бути порожнім")
  @Size(min = 6, max = 255, message = "Пароль повинен бути від 6 до 255 символів")
  private String password;
}
