package com.trailtales.repository;

import com.trailtales.entity.Photo;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types; // Додано для setNull
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
public class PhotoRepository {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<Photo> photoRowMapper =
      (rs, rowNum) -> {
        Photo photo = new Photo();
        photo.setId(rs.getLong("id"));
        photo.setJourneyId(rs.getLong("journey_id"));
        // Перевіряємо, чи стовпець user_id є NULL, перш ніж встановлювати його
        // getObject("user_id", Long.class) краще, ніж getLong, якщо стовпець може бути NULL
        Object userIdObj = rs.getObject("user_id");
        if (userIdObj != null) {
          photo.setUserId(((Number) userIdObj).longValue());
        } else {
          photo.setUserId(null); // Явно встановлюємо null, якщо значення відсутнє
        }
        photo.setFilePath(rs.getString("file_path"));
        photo.setDescription(rs.getString("description"));
        photo.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        photo.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return photo;
      };

  public PhotoRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public Optional<Photo> findById(Long id) {
    String sql =
        "SELECT id, journey_id, user_id, file_path, description, created_at, updated_at FROM photos WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, photoRowMapper, id));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public List<Photo> findByJourneyId(Long journeyId) {
    String sql =
        "SELECT id, journey_id, user_id, file_path, description, created_at, updated_at FROM photos WHERE journey_id = ?";
    return jdbcTemplate.query(sql, photoRowMapper, journeyId);
  }

  public Photo save(Photo photo) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql;

    if (photo.getId() == null) {
      // ВИПРАВЛЕНО: Додано user_id до списку стовпців і значень
      sql =
          "INSERT INTO photos (journey_id, user_id, file_path, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, photo.getJourneyId());
            // Встановлюємо user_id, якщо він не null
            if (photo.getUserId() != null) {
              ps.setLong(2, photo.getUserId());
            } else {
              ps.setNull(2, Types.BIGINT); // Встановлюємо NULL, якщо user_id відсутній
            }
            ps.setString(3, photo.getFilePath());
            ps.setString(4, photo.getDescription());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            return ps;
          },
          keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        photo.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException(
            "Не вдалося отримати згенерований ID після вставки фотографії.");
      }
    } else {
      // ВИПРАВЛЕНО: Додано user_id до UPDATE операції
      sql =
          "UPDATE photos SET journey_id = ?, user_id = ?, file_path = ?, description = ?, updated_at = ? WHERE id = ?";
      jdbcTemplate.update(
          sql,
          photo.getJourneyId(),
          photo.getUserId(), // Передаємо user_id
          photo.getFilePath(),
          photo.getDescription(),
          Timestamp.valueOf(LocalDateTime.now()),
          photo.getId());
    }
    return photo;
  }

  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM photos WHERE id = ?", id);
  }
}
