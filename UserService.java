package com.trailtales.service;

import com.trailtales.dto.UserLoginDto;
import com.trailtales.dto.UserRegistrationDto;
import com.trailtales.entity.User;
import java.util.Optional;

/**
 * Сервіс для управління операціями, пов'язаними з користувачами, такими як реєстрація, вхід та
 * отримання інформації про користувача.
 */
public interface UserService {

  /**
   * Реєструє нового користувача в системі.
   *
   * @param registrationDto об'єкт {@link UserRegistrationDto}, що містить дані для реєстрації.
   * @return створений об'єкт {@link User}.
   * @throws com.trailtales.exception.UserAlreadyExistsException якщо користувач з таким іменем або
   *     email вже існує.
   * @throws IllegalArgumentException якщо вхідні дані не пройшли валідацію.
   */
  User registerUser(UserRegistrationDto registrationDto);

  /**
   * Здійснює вхід користувача в систему.
   *
   * @param loginDto об'єкт {@link UserLoginDto}, що містить ідентифікатор (ім'я користувача або
   *     email) та пароль.
   * @return об'єкт {@link User}, якщо аутентифікація пройшла успішно.
   * @throws com.trailtales.exception.AuthenticationException якщо надано невірний ідентифікатор або
   *     пароль.
   * @throws IllegalArgumentException якщо вхідні дані не пройшли валідацію.
   */
  User loginUser(UserLoginDto loginDto);

  /**
   * Знаходить користувача за його унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор користувача.
   * @return {@link Optional} з користувачем, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  Optional<User> getUserById(Long id);
}
