package com.trailtales.entity;

import java.time.LocalDateTime; // Додано
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
  private Long id;
  private RoleName name;
  private LocalDateTime createdAt; // Додано
  private LocalDateTime updatedAt; // Додано
}
