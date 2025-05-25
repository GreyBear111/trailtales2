package com.trailtales.util;

import org.springframework.stereotype.Component;

@Component // Зробимо це Spring компонентом
public class EmailService {

  /**
   * Перевіряє, чи містить рядок символи '@', що вважається мінімальною перевіркою формату
   * електронної пошти.
   *
   * @param email Рядок електронної пошти для перевірки.
   * @return true, якщо email не порожній і містить '@', інакше false.
   */
  public boolean isValidEmailFormat(String email) {
    return email != null && !email.trim().isEmpty() && email.contains("@");
  }

  // Метод для "відправки" верифікаційного листа тепер просто логуватиме
  public void sendVerificationEmail(String toEmail, String verificationToken) {
    System.out.println(
        "No actual email sending configured. Simulating email verification for: " + toEmail);
    System.out.println("Verification Token (for internal use, not sent): " + verificationToken);
  }
}
