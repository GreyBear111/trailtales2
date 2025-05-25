package com.trailtales.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Утилітарний клас для хешування паролів та перевірки їх відповідності. Використовує алгоритм
 * bcrypt.
 */
public class PasswordHasher {

  /**
   * Хешує наданий пароль за допомогою bcrypt.
   *
   * @param password пароль у відкритому вигляді для хешування.
   * @return хешований рядок пароля.
   */
  public String hashPassword(String password) {
    return BCrypt.hashpw(password, BCrypt.gensalt());
  }

  /**
   * Перевіряє, чи відповідає наданий пароль у відкритому вигляді раніше згенерованому хешу.
   *
   * @param rawPassword пароль у відкритому вигляді.
   * @param encodedPassword хешований пароль для порівняння.
   * @return {@code true}, якщо паролі збігаються, {@code false} в іншому випадку.
   */
  public boolean checkPassword(String rawPassword, String encodedPassword) {
    return BCrypt.checkpw(rawPassword, encodedPassword);
  }
}
