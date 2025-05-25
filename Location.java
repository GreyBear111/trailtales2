package com.trailtales.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
  private Long id;
  private String name;
  private String description; // Додано опис, якщо його ще немає
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
