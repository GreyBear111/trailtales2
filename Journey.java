package com.trailtales.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Journey {
  private Long id;
  private Long userId;
  private User user; // ДОДАЙТЕ ЦЕ: Для завантаження повного об'єкта User
  private String name;
  private String description;
  private LocalDate startDate;
  private LocalDate endDate;

  private Long originLocationId; // ДОДАЙТЕ ЦЕ: Для зберігання ID початкової локації
  private Location originLocation; // ЗМІНІТЬ ТИП: З String на Location

  private Long destinationLocationId; // ДОДАЙТЕ ЦЕ: Для зберігання ID кінцевої локації
  private Location destinationLocation; // ЗМІНІТЬ ТИП: З String на Location

  private Set<User> participants;
  private Set<Tag> tags;
  private List<Event> events;
  private List<Photo> photos;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
