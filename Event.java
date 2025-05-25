package com.trailtales.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
  private Long id;
  private Long journeyId; // Зовнішній ключ до Journey
  private Long locationId; // Зовнішній ключ до Location
  private String name;
  private String description;
  private LocalDate eventDate;
  private LocalTime eventTime;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
