package com.trailtales.repository;

import com.trailtales.entity.Tag;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/** Репозиторій для управління сутностями {@link Tag} у базі даних. */
@Repository
public class TagRepository {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<Tag> tagRowMapper =
      (rs, rowNum) -> {
        Tag tag = new Tag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        tag.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        tag.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return tag;
      };

  /**
   * Конструктор для впровадження залежності {@link JdbcTemplate}.
   *
   * @param jdbcTemplate об'єкт для взаємодії з базою даних.
   */
  public TagRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Знаходить тег за його унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор тегу.
   * @return {@link Optional} з тегом, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  public Optional<Tag> findById(Long id) {
    String sql = "SELECT id, name, created_at, updated_at FROM tags WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, tagRowMapper, id));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Знаходить тег за його назвою.
   *
   * @param name назва тегу.
   * @return {@link Optional} з тегом, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  public Optional<Tag> findByName(String name) {
    String sql = "SELECT id, name, created_at, updated_at FROM tags WHERE name = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, tagRowMapper, name));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Повертає список усіх тегів з бази даних.
   *
   * @return список усіх тегів.
   */
  public List<Tag> findAll() {
    String sql = "SELECT id, name, created_at, updated_at FROM tags";
    return jdbcTemplate.query(sql, tagRowMapper);
  }

  /**
   * Знаходить набір тегів, пов'язаних з певною подорожжю.
   *
   * @param journeyId ID подорожі.
   * @return набір {@link Tag}, пов'язаних із вказаною подорожжю.
   */
  public Set<Tag> findTagsByJourneyId(Long journeyId) {
    String sql =
        "SELECT t.id, t.name, t.created_at, t.updated_at FROM tags t JOIN journey_tags jt ON t.id = jt.tag_id WHERE jt.journey_id = ?";
    return new HashSet<>(jdbcTemplate.query(sql, tagRowMapper, journeyId));
  }

  /**
   * Додає зв'язок між подорожжю та тегом у проміжну таблицю.
   *
   * @param journeyId ID подорожі.
   * @param tagId ID тегу.
   */
  public void addTagToJourney(Long journeyId, Long tagId) {
    String sql = "INSERT INTO journey_tags (journey_id, tag_id) VALUES (?, ?)";
    jdbcTemplate.update(sql, journeyId, tagId);
  }

  /**
   * Видаляє зв'язок між подорожжю та тегом з проміжної таблиці.
   *
   * @param journeyId ID подорожі.
   * @param tagId ID тегу.
   */
  public void removeTagFromJourney(Long journeyId, Long tagId) {
    String sql = "DELETE FROM journey_tags WHERE journey_id = ? AND tag_id = ? ";
    jdbcTemplate.update(sql, journeyId, tagId);
  }

  /**
   * Зберігає або оновлює тег у базі даних. Якщо {@code tag.getId()} є {@code null}, створюється
   * новий тег. В іншому випадку, оновлюється існуючий тег.
   *
   * @param tag об'єкт {@link Tag} для збереження або оновлення.
   * @return збережений або оновлений тег з встановленим ID (якщо новий) та часом оновлення.
   * @throws IllegalStateException якщо не вдалося отримати згенерований ID після вставки нового
   *     тегу.
   */
  public Tag save(Tag tag) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql;

    if (tag.getId() == null) {
      sql = "INSERT INTO tags (name, created_at, updated_at) VALUES (?, ?, ?)";
      LocalDateTime now = LocalDateTime.now();
      tag.setCreatedAt(now);
      tag.setUpdatedAt(now);

      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tag.getName());
            ps.setTimestamp(2, Timestamp.valueOf(tag.getCreatedAt()));
            ps.setTimestamp(3, Timestamp.valueOf(tag.getUpdatedAt()));
            return ps;
          },
          keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        tag.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException(
            "Не вдалося отримати згенерований ID після вставки тегу. Перевірте конфігурацію таблиці та драйвера.");
      }
    } else {
      sql = "UPDATE tags SET name = ?, updated_at = ? WHERE id = ?";
      tag.setUpdatedAt(LocalDateTime.now());
      jdbcTemplate.update(sql, tag.getName(), Timestamp.valueOf(tag.getUpdatedAt()), tag.getId());
    }
    return tag;
  }

  /**
   * Видаляє тег за його унікальним ідентифікатором. Також видаляє всі пов'язані записи з таблиці
   * `journey_tags`.
   *
   * @param id унікальний ідентифікатор тегу для видалення.
   */
  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM journey_tags WHERE tag_id = ?", id);
    jdbcTemplate.update("DELETE FROM tags WHERE id = ?", id);
  }
}
