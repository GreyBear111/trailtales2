package com.trailtales.repository;

import com.trailtales.entity.Event;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
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

@Repository
public class EventRepository {

  private final JdbcTemplate jdbcTemplate;

  /** RowMapper для відображення рядків ResultSet у об'єкти Event. */
  private final RowMapper<Event> eventRowMapper =
      (rs, rowNum) -> {
        Event event = new Event();
        event.setId(rs.getLong("id"));
        event.setJourneyId(rs.getLong("journey_id"));
        event.setName(rs.getString("name"));
        event.setDescription(rs.getString("description"));
        if (rs.getDate("event_date") != null) {
          event.setEventDate(rs.getDate("event_date").toLocalDate());
        }
        if (rs.getTime("event_time") != null) {
          event.setEventTime(rs.getTime("event_time").toLocalTime());
        }
        // Отримуємо location_id, якщо він не NULL
        Long locationId = rs.getObject("location_id", Long.class);
        event.setLocationId(locationId);
        event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        event.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return event;
      };

  /**
   * Конструктор для впровадження залежностей.
   *
   * @param jdbcTemplate JdbcTemplate для взаємодії з базою даних.
   */
  public EventRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Знаходить подію за її ID.
   *
   * @param id ID події.
   * @return Optional, що містить Event, якщо знайдено, або порожній Optional.
   */
  public Optional<Event> findById(Long id) {
    String sql =
        "SELECT id, journey_id, name, description, event_date, event_time, location_id, created_at, updated_at FROM events WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, eventRowMapper, id));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Повертає список усіх подій.
   *
   * @return Список об'єктів Event.
   */
  public List<Event> findAll() {
    String sql =
        "SELECT id, journey_id, name, description, event_date, event_time, location_id, created_at, updated_at FROM events";
    return jdbcTemplate.query(sql, eventRowMapper);
  }

  /**
   * Знаходить події за ID подорожі.
   *
   * @param journeyId ID подорожі.
   * @return Список об'єктів Event, пов'язаних з вказаною подорожжю.
   */
  public List<Event> findByJourneyId(Long journeyId) {
    String sql =
        "SELECT id, journey_id, name, description, event_date, event_time, location_id, created_at, updated_at FROM events WHERE journey_id = ?";
    return jdbcTemplate.query(sql, eventRowMapper, journeyId);
  }

  /**
   * Зберігає або оновлює подію у базі даних.
   *
   * @param event Об'єкт Event для збереження.
   * @return Збережений об'єкт Event з оновленим ID, якщо це нова подія.
   */
  public Event save(Event event) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql;

    if (event.getId() == null) {
      // Вставка нової події
      sql =
          "INSERT INTO events (journey_id, name, description, event_date, event_time, location_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setObject(
                1,
                event.getJourneyId(),
                Types.BIGINT); // Використовуємо setObject для null-able Long
            ps.setString(2, event.getName());
            ps.setString(3, event.getDescription());
            ps.setObject(
                4,
                event.getEventDate() != null ? java.sql.Date.valueOf(event.getEventDate()) : null,
                Types.DATE);
            ps.setObject(
                5,
                event.getEventTime() != null ? java.sql.Time.valueOf(event.getEventTime()) : null,
                Types.TIME);
            ps.setObject(
                6,
                event.getLocationId(),
                Types.BIGINT); // Використовуємо setObject для null-able Long
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
          },
          keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        event.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException("Не вдалося отримати згенерований ID після вставки події.");
      }
    } else {
      // Оновлення існуючої події
      sql =
          "UPDATE events SET journey_id = ?, name = ?, description = ?, event_date = ?, event_time = ?, location_id = ?, updated_at = ? WHERE id = ?";
      jdbcTemplate.update(
          sql,
          event.getJourneyId(),
          event.getName(),
          event.getDescription(),
          event.getEventDate() != null ? java.sql.Date.valueOf(event.getEventDate()) : null,
          event.getEventTime() != null ? java.sql.Time.valueOf(event.getEventTime()) : null,
          event.getLocationId(), // Встановлюємо locationId напряму
          Timestamp.valueOf(LocalDateTime.now()),
          event.getId());
    }
    return event;
  }

  /**
   * Видаляє подію за її ID.
   *
   * @param id ID події, яку потрібно видалити.
   */
  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM events WHERE id = ?", id);
  }
}
