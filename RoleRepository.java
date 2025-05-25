package com.trailtales.repository;

import com.trailtales.entity.Role;
import com.trailtales.entity.RoleName;
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

/** Репозиторій для управління сутностями {@link Role} у базі даних. */
@Repository
public class RoleRepository {

  private final JdbcTemplate jdbcTemplate;

  private final RowMapper<Role> roleRowMapper =
      (rs, rowNum) -> {
        Role role = new Role();
        role.setId(rs.getLong("id"));
        role.setName(RoleName.valueOf(rs.getString("name")));
        role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        role.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return role;
      };

  /**
   * Конструктор для впровадження залежності {@link JdbcTemplate}.
   *
   * @param jdbcTemplate об'єкт для взаємодії з базою даних.
   */
  public RoleRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * Повертає RowMapper для об'єктів {@link Role}. Використовується в інших репозиторіях (наприклад,
   * {@link UserRepository}) для завантаження ролей.
   *
   * @return {@link RowMapper} для {@link Role}.
   */
  public RowMapper<Role> getRoleRowMapper() {
    return roleRowMapper;
  }

  /**
   * Знаходить роль за її унікальним ідентифікатором.
   *
   * @param id унікальний ідентифікатор ролі.
   * @return {@link Optional} з роллю, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  public Optional<Role> findById(Long id) {
    String sql = "SELECT id, name, created_at, updated_at FROM roles WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, roleRowMapper, id));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Знаходить роль за її назвою (використовуючи {@link RoleName}).
   *
   * @param name назва ролі типу {@link RoleName}.
   * @return {@link Optional} з роллю, якщо знайдено, або порожній {@link Optional}, якщо ні.
   */
  public Optional<Role> findByName(RoleName name) {
    String sql = "SELECT id, name, created_at, updated_at FROM roles WHERE name = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, roleRowMapper, name.name()));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * Повертає список усіх ролей з бази даних.
   *
   * @return список усіх ролей.
   */
  public List<Role> findAll() {
    String sql = "SELECT id, name, created_at, updated_at FROM roles";
    return jdbcTemplate.query(sql, roleRowMapper);
  }

  /**
   * Знаходить набір ролей, призначених певному користувачеві.
   *
   * @param userId ID користувача.
   * @return набір {@link Role}, призначених вказаному користувачеві.
   */
  public Set<Role> findRolesByUserId(Long userId) {
    String sql =
        "SELECT r.id, r.name, r.created_at, r.updated_at FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = ?";
    List<Role> rolesList = jdbcTemplate.query(sql, roleRowMapper, userId);
    return new HashSet<>(rolesList);
  }

  /**
   * Зберігає або оновлює роль у базі даних. Якщо {@code role.getId()} є {@code null}, створюється
   * нова роль. В іншому випадку, оновлюється існуюча роль (зазвичай тільки час оновлення).
   *
   * @param role об'єкт {@link Role} для збереження або оновлення.
   * @return збережена або оновлена роль з встановленим ID (якщо новий) та часом оновлення.
   * @throws IllegalStateException якщо не вдалося отримати згенерований ID після вставки нової
   *     ролі.
   */
  public Role save(Role role) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql;

    if (role.getId() == null) {
      sql = "INSERT INTO roles (name, created_at, updated_at) VALUES (?, ?, ?)";
      LocalDateTime now = LocalDateTime.now();
      role.setCreatedAt(now);
      role.setUpdatedAt(now);

      jdbcTemplate.update(
          connection -> {
            PreparedStatement ps =
                connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, role.getName().name());
            ps.setTimestamp(2, Timestamp.valueOf(role.getCreatedAt()));
            ps.setTimestamp(3, Timestamp.valueOf(role.getUpdatedAt()));
            return ps;
          },
          keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        role.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException("Не вдалося отримати згенерований ID після вставки ролі.");
      }
    } else {
      sql = "UPDATE roles SET name = ?, updated_at = ? WHERE id = ?";
      role.setUpdatedAt(LocalDateTime.now());
      jdbcTemplate.update(
          sql, role.getName().name(), Timestamp.valueOf(role.getUpdatedAt()), role.getId());
    }
    return role;
  }

  /**
   * Видаляє роль за її унікальним ідентифікатором. Також видаляє всі пов'язані записи з таблиці
   * `user_roles`.
   *
   * @param id унікальний ідентифікатор ролі для видалення.
   */
  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM user_roles WHERE role_id = ?", id);
    jdbcTemplate.update("DELETE FROM roles WHERE id = ?", id);
  }
}
