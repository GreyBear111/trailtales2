package com.trailtales.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Генерує геттери, сеттери, toString, equals, hashCode
@NoArgsConstructor // Генерує конструктор без аргументів
@AllArgsConstructor // Генерує конструктор з усіма аргументами
public class User {
  private Long id;
  private String username;
  private String email;
  private String passwordHash;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Set<Role> roles =
      new HashSet<>(); // Ініціалізуємо тут, щоб уникнути NullPointerException, навіть з Lombok

  // Додатковий метод, який Lombok не генерує, але він потрібен для додавання ролей
  public void addRole(Role role) {
    if (this.roles == null) { // Запасний варіант, хоча ініціалізація вище має запобігти цьому
      this.roles = new HashSet<>();
    }
    this.roles.add(role);
  }
}
