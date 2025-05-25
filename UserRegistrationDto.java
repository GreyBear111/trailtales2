package com.trailtales.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationDto {

  @NotBlank(message = "Ім'я користувача не може бути порожнім")
  @Size(min = 3, max = 50, message = "Ім'я користувача повинно бути від 3 до 50 символів")
  private String username;

  @NotBlank(message = "Email не може бути порожнім")
  @Email(message = "Некоректний формат Email")
  @Size(max = 100, message = "Email не може перевищувати 100 символів")
  private String email;

  @NotBlank(message = "Пароль не може бути порожнім")
  @Size(min = 6, max = 255, message = "Пароль повинен бути від 6 до 255 символів")
  private String password;
}
