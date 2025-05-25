package com.trailtales.entity;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag {
  private Long id;
  private String name;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
