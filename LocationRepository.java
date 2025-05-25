package com.trailtales.repository;

import com.trailtales.entity.Location;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Репозиторій для управління сутностями {@link Location} у базі даних. */
@Repository
public class LocationRepository {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<Location> locationRowMapper =
      (rs, rowNum) -> {
        Location location = new Location();
        location.setId(rs.getLong("id"));
        location.setName(rs.getString("name"));
        location.setDescription(rs.getString("description"));
        location.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        location.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return location;
      };

  /**
   * Конструктор для впровадження залежності {@link JdbcTemplate}.
   *
   * @param jdbcTemplate об'єкт для взаємодії з базою даних.
   */
  public LocationRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Знаходить локацію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор локації.
   * @return {@link Optional} з локацією, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  public Optional<Location> findById(Long id) {
    String sql = "SELECT id, name, description, created_at, updated_at FROM locations WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, locationRowMapper, id));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Знаходить локацію за її назвою.
   *
   * @param name назва локації.
   * @return {@link Optional} з локацією, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  public Optional<Location> findByName(String name) {
    String sql =
        "SELECT id, name, description, created_at, updated_at FROM locations WHERE name = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, locationRowMapper, name));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Повертає список усіх локацій з бази даних.
   *
   * @return список усіх локацій.
   */
  public List<Location> findAll() {
    String sql = "SELECT id, name, description, created_at, updated_at FROM locations";
    return jdbcTemplate.query(sql, locationRowMapper);
  }

  /**
   * Зберігає або оновлює локацію у базі даних. Якщо {@code location.getId()} є {@code null},
   * створюється нова локація. В іншому випадку, оновлюється існуюча локація.
   *
   * @param location об'єкт {@link Location} для збереження або оновлення.
   * @return збережена або оновлена локація з встановленим ID (якщо новий) та часом оновлення.
   * @throws IllegalStateException якщо не вдалося отримати згенерований ID після вставки нової
   *     локації.
   */
  public Location save(Location location) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql;

    if (location.getId() == null) {
      sql = "INSERT INTO locations (name, description, created_at, updated_at) VALUES (?, ?, ?, ?)";
      LocalDateTime now = LocalDateTime.now();
      location.setCreatedAt(now);
      location.setUpdatedAt(now);

      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, location.getName());
            ps.setString(2, location.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(location.getCreatedAt()));
            ps.setTimestamp(4, Timestamp.valueOf(location.getUpdatedAt()));
            return ps;
          },
          keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        location.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException(
            "Не вдалося отримати згенерований ID після вставки локації.");
      }
    } else {
      sql = "UPDATE locations SET name = ?, description = ?, updated_at = ? WHERE id = ?";
      location.setUpdatedAt(LocalDateTime.now());
      jdbcTemplate.update(
          sql,
          location.getName(),
          location.getDescription(),
          Timestamp.valueOf(location.getUpdatedAt()),
          location.getId());
    }
    return location;
  }

  /**
   * Видаляє локацію за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор локації для видалення.
   */
  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM locations WHERE id = ?", id);
  }
}
