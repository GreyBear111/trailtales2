package com.trailtales.repository;

import com.trailtales.entity.Role;
import com.trailtales.entity.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbcTemplate;
  private final RoleRepository roleRepository;

  private final RowMapper<User> userRowMapper;

  public UserRepository(JdbcTemplate jdbcTemplate, RoleRepository roleRepository) {
    this.jdbcTemplate = jdbcTemplate;
    this.roleRepository = roleRepository;

    this.userRowMapper =
            (rs, rowNum) -> {
              User user = new User();
              user.setId(rs.getLong("id"));
              user.setUsername(rs.getString("username"));
              user.setEmail(rs.getString("email"));
              user.setPasswordHash(rs.getString("password_hash"));
              user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
              user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
              user.setRoles(findRolesByUserId(user.getId()));
              return user;
            };
  }

  public Optional<User> findById(Long id) {
    String sql =
            "SELECT id, username, email, password_hash, created_at, updated_at FROM users WHERE id = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public Optional<User> findByUsername(String username) {
    String sql =
            "SELECT id, username, email, password_hash, created_at, updated_at FROM users WHERE username = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, username));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public Optional<User> findByEmail(String email) {
    String sql =
            "SELECT id, username, email, password_hash, created_at, updated_at FROM users WHERE email = ?";
    try {
      return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, email));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public Optional<User> findByUsernameOrEmail(String identifier) {
    String sql =
            "SELECT id, username, email, password_hash, created_at, updated_at FROM users WHERE username = ? OR email = ?";
    try {
      return Optional.ofNullable(
              jdbcTemplate.queryForObject(sql, userRowMapper, identifier, identifier));
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  public List<User> findAll() {
    String sql = "SELECT id, username, email, password_hash, created_at, updated_at FROM users";
    return jdbcTemplate.query(sql, userRowMapper);
  }

  public User save(User user) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    String sql;
    LocalDateTime now = LocalDateTime.now();

    if (user.getId() == null) {
      sql =
              "INSERT INTO users (username, email, password_hash, created_at, updated_at) VALUES (?, ?, ?, ?, ?)";
      user.setCreatedAt(now);
      user.setUpdatedAt(now);

      jdbcTemplate.update(
              connection -> {
                PreparedStatement ps =
                        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getEmail());
                ps.setString(3, user.getPasswordHash());
                ps.setTimestamp(4, Timestamp.valueOf(user.getCreatedAt()));
                ps.setTimestamp(5, Timestamp.valueOf(user.getUpdatedAt()));
                return ps;
              },
              keyHolder);

      Map<String, Object> keys = keyHolder.getKeys();
      if (keys != null && keys.containsKey("id")) {
        user.setId(((Number) keys.get("id")).longValue());
      } else {
        throw new IllegalStateException(
                "Не вдалося отримати згенерований ID після вставки користувача.");
      }
    } else {
      sql =
              "UPDATE users SET username = ?, email = ?, password_hash = ?, updated_at = ? WHERE id = ?";
      user.setUpdatedAt(now);
      jdbcTemplate.update(
              sql,
              user.getUsername(),
              user.getEmail(),
              user.getPasswordHash(),
              Timestamp.valueOf(user.getUpdatedAt()),
              user.getId());
    }

    if (user.getId() != null && user.getRoles() != null) {
      jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());
      for (Role role : user.getRoles()) {
        if (role.getId() == null && role.getName() != null) { // Якщо роль нова і має тільки ім'я
          Role persistedRole = roleRepository.findByName(role.getName())
                  .orElseThrow(() -> new IllegalStateException(
                          "Роль " + role.getName() + " не знайдена в базі даних."));
          jdbcTemplate.update(
                  "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", user.getId(), persistedRole.getId());
        } else if (role.getId() != null) { // Якщо роль вже має ID
          jdbcTemplate.update(
                  "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", user.getId(), role.getId());
        } else {
          throw new IllegalStateException(
                  "Неможливо зберегти роль без ID або імені для користувача " + user.getUsername());
        }
      }
    }
    return user;
  }

  public void deleteById(Long id) {
    jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", id);
    jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
  }

  private Set<Role> findRolesByUserId(Long userId) {
    String sql =
            "SELECT r.id, r.name, r.created_at, r.updated_at FROM roles r JOIN user_roles ur ON r.id = ur.role_id WHERE ur.user_id = ?";
    return new HashSet<>(jdbcTemplate.query(sql, roleRepository.getRoleRowMapper(), userId));
  }
}